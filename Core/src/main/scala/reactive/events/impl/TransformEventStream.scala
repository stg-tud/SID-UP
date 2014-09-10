package reactive
package events
package impl

import reactive.impl.SingleDependentReactive

class TransformEventStream[A, B](val dependency: EventStream[B], private val op: Option[B] => Option[A]) extends DependentEventStreamImpl[A] with SingleDependentReactive {
  protected def reevaluate(transaction: Transaction): Option[A] = op(dependency.pulse(transaction))
}
