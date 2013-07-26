package reactive
package events
package impl

import reactive.signals.Signal
import util.TicketAccumulator
import reactive.impl.SingleDependentReactive

class ChangesEventStream[A](val dependency: Signal[A]) extends EventStreamImpl[A] with SingleDependentReactive[A] {
  protected def calculatePulse(transaction: Transaction): Option[A] = dependency.pulse(transaction)
}