package reactive
package events
package impl

import util.TicketAccumulator
import reactive.impl.SingleDependentReactive

class FilteredEventStream[A](val dependency: EventStream[A], private val op: A => Boolean) extends EventStreamImpl[A] with SingleDependentReactive[A] {
  protected def calculatePulse(transaction: Transaction): Option[A] = dependency.pulse(transaction).filter(op)
}
