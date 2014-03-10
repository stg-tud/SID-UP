package reactive.events

import reactive.impl.DynamicDependentReactive
import reactive.{Reactive, Transaction}
import reactive.signals.Signal
import reactive.events.impl.DependentEventStreamImpl

/**
 * Takes a sequence of events and turns it into an event firing every time one of the original events changes
 */
class TransposeEventStream[A](events: Signal[Seq[EventStream[A]]]) extends DependentEventStreamImpl[Seq[A]] with DynamicDependentReactive {
  override protected def reevaluate(transaction: Transaction): Option[Seq[A]] =
    // get the flat list of pulses, if none changed, we also propagate no change
    events.value(transaction).map(_.pulse(transaction)).flatten match {
      case Seq()    => None
      case notEmpty => Some(notEmpty)
    }

  override protected def dependencies(transaction: Transaction): Set[Reactive[_, _]] = events.value(transaction).toSet + events
}
