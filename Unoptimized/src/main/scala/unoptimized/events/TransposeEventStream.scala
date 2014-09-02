package unoptimized.events

import unoptimized.impl.DynamicDependentReactive
import unoptimized.{Reactive, Transaction}
import unoptimized.signals.Signal
import unoptimized.events.impl.DependentEventStreamImpl

/**
 * Takes a sequence of events and turns it into an event firing every time one of the original events changes
 */
class TransposeEventStream[A](events: Signal[Iterable[EventStream[A]]]) extends DependentEventStreamImpl[Iterable[A]] with DynamicDependentReactive {
  override protected def reevaluate(transaction: Transaction): Option[Iterable[A]] = {
    // get the flat list of pulses, if none changed, we also propagate no change
    val pulses = events.value(transaction).map(_.pulse(transaction)).flatten
    if (pulses.isEmpty) None else Some(pulses)
  }

  override protected def dependencies(transaction: Transaction): Set[Reactive[_, _]] = events.value(transaction).toSet + events
}

