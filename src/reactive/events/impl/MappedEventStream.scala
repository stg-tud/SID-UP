package reactive
package events
package impl

import util.TicketAccumulator
import reactive.impl.SingleDependentReactive

class MappedEventStream[A, B](private val from: EventStream[B], private val op: B => A) extends EventStreamImpl[A] with SingleDependentReactive[A] {
  override val dependency = from
  protected def calculatePulse(transaction: Transaction): Option[A] = from.pulse(transaction).map(op)
}
