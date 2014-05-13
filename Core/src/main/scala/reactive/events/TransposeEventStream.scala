package reactive.events

import reactive.impl.DynamicDependentReactive
import reactive.{Reactive, Transaction}
import reactive.signals.Signal
import reactive.events.impl.DependentEventStreamImpl
import scala.concurrent.stm.InTxn

/**
 * Takes a sequence of events and turns it into an event firing every time one of the original events changes
 */
class TransposeEventStream[A](events: Signal[Iterable[EventStream[A]]], tx: InTxn) extends DynamicDependentReactive(tx) with DependentEventStreamImpl[Iterable[A]] {
  override protected def reevaluate(tx: InTxn): Option[Iterable[A]] = {
    // get the flat list of pulses, if none changed, we also propagate no change
    val pulses = events.now(tx).map(_.pulse(tx).asOption).flatten
    if (pulses.isEmpty) None else Some(pulses)
  }

  override protected def dependencies(tx: InTxn): Set[Reactive[_, _]] = events.now(tx).toSet + events
}
