package elmish
package events
package impl

import elmish.signals.Signal
import elmish.impl.SingleDependentReactive

class ChangesEventStream[A](val dependency: Signal[A]) extends DependentEventStreamImpl[A] with SingleDependentReactive {
  protected def reevaluatePulse(transaction: Transaction): Option[A] = dependency.pulse(transaction)
}
