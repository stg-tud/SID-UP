package elmish
package events
package impl

import elmish.impl.SingleDependentReactive

class MappedEventStream[A, B](val dependency: EventStream[B], private val op: B => A) extends DependentEventStreamImpl[A] with SingleDependentReactive {
  protected def reevaluatePulse(transaction: Transaction): Option[A] = dependency.pulse(transaction).map(op)
}
