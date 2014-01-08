package reactive
package events
package impl

import reactive.signals.Signal
import util.TicketAccumulator
import reactive.impl.SingleDependentReactive

class ChangesEventStream[A](val dependency: Signal[A]) extends DependentEventStreamImpl[A] with SingleDependentReactive[A, Reactive.IDENTITY, Reactive.UNIT, Reactive.IDENTITY, EventStream] {
  protected def reevaluatePulse(transaction: Transaction): Option[A] = dependency.pulse(transaction)
}