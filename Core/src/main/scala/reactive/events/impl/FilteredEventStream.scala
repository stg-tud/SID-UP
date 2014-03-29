package reactive
package events
package impl

import reactive.impl.SingleDependentReactive

class FilteredEventStream[A](val dependency: EventStream[A], private val op: A => Boolean) extends DependentEventStreamImpl[A] with SingleDependentReactive {
  protected def reevaluate(transaction: Transaction): Option[A] = dependency.pulse(transaction).value.filter(op)
}
