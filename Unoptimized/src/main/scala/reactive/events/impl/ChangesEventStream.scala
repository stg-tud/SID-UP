package reactive
package events
package impl

import reactive.signals.Signal
import reactive.impl.SingleDependentReactive

class ChangesEventStream[A](val dependency: Signal[A]) extends DependentEventStreamImpl[A] with SingleDependentReactive {
  protected def reevaluate(transaction: Transaction): Option[A] = dependency.pulse(transaction)
}
