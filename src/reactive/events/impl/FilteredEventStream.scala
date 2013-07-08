package reactive
package events
package impl

import util.TicketAccumulator
import reactive.impl.SingleDependentReactive

class FilteredEventStream[A](private val from: EventStream[A], private val op: A => Boolean) extends EventStreamImpl[A] with SingleDependentReactive[A] {
  override val dependency = from
  protected def calculatePulse(transaction: Transaction): Option[A] = from.pulse(transaction).filter(op)
}
