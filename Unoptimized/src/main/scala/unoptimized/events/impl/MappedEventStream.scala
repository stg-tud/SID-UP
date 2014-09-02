package unoptimized
package events
package impl

import unoptimized.impl.SingleDependentReactive

class MappedEventStream[A, B](val dependency: EventStream[B], private val op: B => A) extends DependentEventStreamImpl[A] with SingleDependentReactive {
  protected def reevaluate(transaction: Transaction): Option[A] = dependency.pulse(transaction).map(op)
}
