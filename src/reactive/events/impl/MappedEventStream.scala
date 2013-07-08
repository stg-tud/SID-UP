package reactive
package events
package impl

import util.TicketAccumulator
import reactive.impl.SingleDependentReactive

class MappedEventStream[A, B](val dependency: EventStream[B], private val op: B => A) extends EventStreamImpl[A] with SingleDependentReactive[A] {
  protected def calculatePulse(transaction: Transaction): Option[A] = dependency.pulse(transaction).map(op)
}
