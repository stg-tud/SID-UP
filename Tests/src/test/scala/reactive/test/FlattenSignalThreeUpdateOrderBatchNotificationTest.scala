package reactive.test

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import reactive.signals.Var
import reactive.testtools.MessageBuffer
import reactive.signals.Signal
import reactive.testtools.NotificationLog
import reactive.TransactionBuilder
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Success
import org.scalatest.Tag
import reactive.Reactive
import reactive.Transaction
import scala.collection.mutable
import reactive.signals.impl.FlattenSignal

trait IncomingMessageBuffer extends Reactive.Dependant {
  val messages = mutable.MutableList[(Transaction, Boolean, Boolean)]()
  abstract override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean) {
    messages.synchronized {
      messages += ((transaction, sourceDependenciesChanged, pulsed));
    }
  }

  def releaseQueue() {
    messages.synchronized {
      val release = messages.toList;
      messages.clear()
      release
    }.foreach {
      case (transaction, sourceDependenciesChanged, pulsed) =>
        super.apply(transaction, sourceDependenciesChanged, pulsed)
    }
  }
}

class FlattenSignalThreeUpdateOrderTestNotificationOnly extends FunSuite with BeforeAndAfter {
  var inner1: Var[Int] = _
  var inner1Buffered: MessageBuffer[Int] = _
  var inner2: Var[Int] = _
  var inner2Buffered: MessageBuffer[Int] = _
  var outer: Var[Signal[Int]] = _
  var outerBuffered: MessageBuffer[Signal[Int]] = _
  var flattened: Signal[Int] with IncomingMessageBuffer = _
  var log: NotificationLog[Int] = _
  var commitFuture: Future[Unit] = _

  before {
    inner1 = Var(123)
    inner1Buffered = new MessageBuffer(inner1)
    outer = Var(inner1Buffered)
    outerBuffered = new MessageBuffer(outer)
    flattened = new FlattenSignal(outerBuffered) with IncomingMessageBuffer
    log = new NotificationLog(flattened)

    expectResult(123) { flattened.now }
    expectResult(Set(inner1.uuid, outer.uuid)) { flattened.sourceDependencies(null) }

    inner2 = Var(234)
    inner2Buffered = new MessageBuffer(inner2)
    val transaction = new TransactionBuilder()
    transaction.set(inner1, 0);
    transaction.set(outer, inner2Buffered);
    transaction.set(inner2, 5);
    commitFuture = future {
      transaction.commit;
    }
    Thread.sleep(100);
  }

  override def test(testName: String, testTags: Tag*)(testFun: => Unit) {
    super.test(testName, testTags: _*) {
      testFun
      expectResult(()) { Await.result(commitFuture, duration.Duration.Inf) }
    }
  }

  List("old", "new", "outer").permutations.foreach { permutation =>
    test(permutation.mkString(", ")) {
      permutation.foreach { name =>
        val relevant = name match {
          case "old" =>
            inner1Buffered.releaseQueue
            false
          case "new" =>
            inner2Buffered.releaseQueue
            true
          case "outer" =>
            outerBuffered.releaseQueue
            true
        }
      }
      flattened.releaseQueue()
      
      expectResult(5) { flattened.now }
      expectResult(Set(inner2.uuid, outer.uuid)) { flattened.sourceDependencies(null) }
      expectResult(1) { log.size }
      val notification1 = log.dequeue
      expectResult(true) { notification1.valueChanged }
      expectResult(5) { notification1.newValue }
      expectResult(true) { notification1.sourceDependenciesChanged }
      expectResult(Set(inner2.uuid, outer.uuid)) { notification1.newSourceDependencies }
    }
  }
}
