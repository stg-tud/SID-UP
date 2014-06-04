package reactive.transactionality.implicitly

import reactive.signals.Signal
import reactive.events.EventStream
import scala.language.implicitConversions

object single {
  implicit def singleSignal[A](signal: Signal[A]): Signal.View[A] = signal.single
  implicit def singleEventStream[A](eventStream: EventStream[A]): EventStream.View[A] = eventStream.single
}
