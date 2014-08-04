package reactive.test

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import reactive.signals.Var
import reactive.testtools.{MessageBuffer, NotificationLog}
import reactive.signals.Signal
import reactive.TransactionBuilder
import scala.concurrent._
import ExecutionContext.Implicits.global
import org.scalatest.Tag
import reactive.signals.impl.FlattenSignal

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
    inner1Buffered = scala.concurrent.stm.atomic { new MessageBuffer("inner1", inner1, _) }
    outer = Var(inner1Buffered)
    outerBuffered = scala.concurrent.stm.atomic { new MessageBuffer("outer", outer, _) }
    flattened = outerBuffered.single.flatten
    log = new NotificationLog(flattened)

    assertResult(123) { flattened.single.now }
    assertResult(Set(inner1.uuid, outer.uuid)) { flattened.single.sourceDependencies }

    inner2 = Var(234)
    inner2Buffered = scala.concurrent.stm.atomic { new MessageBuffer("inner2", inner2, _) }
    val transaction = new TransactionBuilder()
    transaction.set(inner1, 0)
    transaction.set(outer, inner2Buffered)
    transaction.set(inner2, 5)
    commitFuture = Future {
      transaction.commit()
    }
  }

  List("old", "new", "outer").permutations.foreach { permutation =>
    test(permutation.mkString(", ")) {
      permutation.foreach {
        case "old" =>
          inner1Buffered.releaseQueue()
        case "new" =>
          inner2Buffered.releaseQueue()
        case "outer" =>
          outerBuffered.releaseQueue()
      }
      assertResult(()) { Await.result(commitFuture, duration.Duration.Inf) }
      
      assertResult(5) { flattened.single.now }
      assertResult(Set(inner2.uuid, outer.uuid)) { flattened.single.sourceDependencies }
      assertResult(1) { log.size }
      val notification1 = log.dequeue()
      assertResult(true) { notification1.valueChanged }
      assertResult(5) { notification1.newValue }
      assertResult(true) { notification1.sourceDependenciesChanged }
      assertResult(Set(inner2.uuid, outer.uuid)) { notification1.newSourceDependencies }
    }
  }
}
