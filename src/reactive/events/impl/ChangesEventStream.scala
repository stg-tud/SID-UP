package reactive
package events
package impl

import reactive.signals.Signal
import util.TicketAccumulator
import reactive.impl.SingleDependentReactive

class ChangesEventStream[A](private val from: Signal[A]) extends EventStreamImpl[A] with SingleDependentReactive[A] {
  override val dependency = from
  protected def calculatePulse(transaction: Transaction): Option[A] = from.pulse(transaction)
}