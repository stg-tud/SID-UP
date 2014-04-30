package reactive
package events
package impl

import reactive.impl.SingleDependentReactive

class PartiallyMappedEventStream[A, B](val dependency: EventStream[B], private val op: PartialFunction[B, A]) extends DependentEventStreamImpl[A] with SingleDependentReactive {
  protected def reevaluate(transaction: Transaction): Option[A] = dependency.pulse(transaction).asOption.collect(op)
}
