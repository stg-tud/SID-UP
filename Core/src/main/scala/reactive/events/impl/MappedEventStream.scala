package reactive
package events
package impl

import reactive.impl.SingleDependentReactive

class MappedEventStream[A, B](val dependency: EventStream[B], private val op: B => A) extends DependentEventStreamImpl[A] with SingleDependentReactive[A, Reactive.IDENTITY, Reactive.UNIT, Reactive.IDENTITY, EventStream] {
  protected def reevaluatePulse(transaction: Transaction): Option[A] = dependency.pulse(transaction).map(op)
}
