package reactive.events

import reactive.impl.MultiDependentReactive
import reactive.{Reactive, Transaction}
import reactive.events.impl.DependentEventStreamImpl

/**
 * Takes a sequence of events and turns it into an event firing every time one of the original events changes
 */
class TransposeEventStream[A](events: Seq[EventStream[A]]) extends {
  override val dependencies = events.toSet[Reactive.Dependency]
} with DependentEventStreamImpl[Seq[A]] with MultiDependentReactive {
  override def reevaluateValue(transaction: Transaction) = events.map(_.value(transaction))

  protected def reevaluatePulse(transaction: Transaction): Option[Seq[A]] = Option(events.map(_.pulse(transaction)).flatten)
}
