package reactive
package events
package impl

import reactive.impl.SingleDependentReactive

class FilteredEventStream[A](val dependency: EventStream[A], private val op: A => Boolean) extends DependentEventStreamImpl[A] with SingleDependentReactive[A, Reactive.IDENTITY, Reactive.UNIT, Reactive.IDENTITY, EventStream] {
  protected def reevaluatePulse(transaction: Transaction): Option[A] = dependency.pulse(transaction).filter(op)
}
