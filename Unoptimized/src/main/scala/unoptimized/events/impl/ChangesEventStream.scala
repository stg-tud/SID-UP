package unoptimized
package events
package impl

import unoptimized.signals.Signal
import unoptimized.impl.SingleDependentReactive

class ChangesEventStream[A](val dependency: Signal[A]) extends DependentEventStreamImpl[A] with SingleDependentReactive {
  protected def reevaluate(transaction: Transaction): Option[A] = dependency.pulse(transaction)
}
