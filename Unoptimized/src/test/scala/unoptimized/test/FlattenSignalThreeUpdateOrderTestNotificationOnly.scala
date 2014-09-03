package unoptimized.test

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import unoptimized.signals.Var
import unoptimized.testtools.{MessageBuffer, NotificationLog}
import unoptimized.signals.Signal
import unoptimized.TransactionBuilder
import scala.concurrent._
import ExecutionContext.Implicits.global
import org.scalatest.Tag
import unoptimized.signals.impl.FlattenSignal

class FlattenSignalThreeUpdateOrderTestNotificationOnly extends FunSuite with BeforeAndAfter {
  var inner1: Var[Int] = _
  var inner1Buffered: MessageBuffer[Int] = _
  var inner2: Var[Int] = _
  var inner2Buffered: MessageBuffer[Int] = _
  var outer: Var[Signal[Int]] = _
  var outerBuffered: MessageBuffer[Signal[Int]] = _
  var flattened: Signal[Int] = _
  var log: NotificationLog[Int] = _
  var commitFuture: Future[Unit] = _

  before {
    inner1 = Var(123)
    inner1Buffered = new MessageBuffer(inner1)
    outer = Var(inner1Buffered)
    outerBuffered = new MessageBuffer(outer)
    flattened = new FlattenSignal(outerBuffered)
    log = new NotificationLog(flattened)

    assertResult(123) { flattened.now }
    assertResult(Set(inner1.uuid, outer.uuid)) { flattened.sourceDependencies(null) }

    inner2 = Var(234)
    inner2Buffered = new MessageBuffer(inner2)
    val transaction = new TransactionBuilder()
    transaction.set(inner1, 0)
    transaction.set(outer, inner2Buffered)
    transaction.set(inner2, 5)
    commitFuture = Future {
      transaction.commit()
    }
    Thread.sleep(100)
  }

  List("old", "new", "outer").permutations.foreach { permutation =>
    test(permutation.mkString(", ")) {
      permutation.foreach { name =>
        val relevant = name match {
          case "old" =>
            inner1Buffered.releaseQueue()
            false
          case "new" =>
            inner2Buffered.releaseQueue()
            true
          case "outer" =>
            outerBuffered.releaseQueue()
            true
        }
      }
      assertResult(()) { Await.result(commitFuture, duration.Duration.Inf) }
      
      assertResult(5) { flattened.now }
      assertResult(Set(inner2.uuid, outer.uuid)) { flattened.sourceDependencies(null) }
      assertResult(1) { log.size }
      val notification1 = log.dequeue()
      assertResult(true) { notification1.valueChanged }
      assertResult(5) { notification1.newValue }
      assertResult(true) { notification1.sourceDependenciesChanged }
      assertResult(Set(inner2.uuid, outer.uuid)) { notification1.newSourceDependencies }
    }
  }
}
