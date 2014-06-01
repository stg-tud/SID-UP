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
import org.scalatest.Tag

class FlattenSignalThreeUpdateOrderTest extends FunSuite with BeforeAndAfter {
  var inner1: Var[Int] = _
  var inner1Buffered: MessageBuffer[Int] = _
  var inner2: Var[Int] = _
  var inner2Buffered: MessageBuffer[Int] = _
  var outer: Var[Signal[Int]] = _
  var outerBuffered: MessageBuffer[Signal[Int]] = _
  var flattened: Signal[Int] = _
  var log: NotificationLog[Int] = _
  var commitFuture: Future[Unit] = _

  def expectSilent() = {
    assertResult(0) { log.size }
  }

  def expectNotification() = {
    assertResult(1) { log.size }
    val notification1 = log.dequeue()
    assertResult(5) { flattened.single.now }
    assertResult(Set(inner2.uuid, outer.uuid)) { flattened.single.sourceDependencies }
    assertResult(true) { notification1.valueChanged }
    assertResult(5) { notification1.newValue }
    assertResult(true) { notification1.sourceDependenciesChanged }
    assertResult(Set(inner2.uuid, outer.uuid)) { notification1.newSourceDependencies }
  }

  before {
    inner1 = Var(123)
    inner1Buffered = scala.concurrent.stm.atomic { new MessageBuffer("old", inner1, _) }
    outer = Var(inner1Buffered)
    outerBuffered = scala.concurrent.stm.atomic { new MessageBuffer("outer", outer, _) }
    flattened = outerBuffered.single.flatten
    log = new NotificationLog(flattened)

    assertResult(123) { flattened.single.now }
    assertResult(Set(inner1.uuid, outer.uuid)) { flattened.single.sourceDependencies }

    inner2 = Var(234)
    inner2Buffered = scala.concurrent.stm.atomic { new MessageBuffer("new", inner2, _) }
    val transaction = new TransactionBuilder()
    transaction.set(inner1, 0)
    transaction.set(outer, inner2Buffered)
    transaction.set(inner2, 5)
    commitFuture = future {
      transaction.commit()
    }
  }

  override def test(testName: String, testTags: Tag*)(testFun: => Unit) {
    super.test(testName, testTags: _*) {
      testFun
      assertResult(()) { Await.result(commitFuture, duration.Duration.Inf) }
    }
  }

  List("old", "new", "outer").permutations.foreach { permutation =>
    test(permutation.mkString(", ")) {
      permutation.foreach { name =>
        name match {
          case "old" =>
            inner1Buffered.releaseQueue()
          case "new" =>
            inner2Buffered.releaseQueue()
          case "outer" =>
            outerBuffered.releaseQueue()
        }
      }
      Await.ready(commitFuture, duration.Duration.Inf)
      expectNotification()
    }
  }
}
