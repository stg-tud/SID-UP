//package reactive
//package testtools
//
//import util.TicketAccumulator
//import scala.concurrent._
//import ExecutionContext.Implicits.global
//import reactive.signals.impl.SignalImpl
//import reactive.signals.Signal
//
//class FakeNetwork[A](input: Signal[A]) extends SignalImpl[A](input.sourceDependencies, input.now) with Signal.Dependant[A] {
//  input.addDependant(None, this);
//  override def notify(replyChannel : TicketAccumulator.Receiver, notification: Signal.Notification[A]) {
//    future {
//      Thread.sleep(500)
//      publish(new Signal.Notification(notification.transaction, notification.sourceDependenciesUpdate.pingTo(_sourceDependencies), notification.pulse.pingTo(value)), replyChannel)
//    }
//  }
//}