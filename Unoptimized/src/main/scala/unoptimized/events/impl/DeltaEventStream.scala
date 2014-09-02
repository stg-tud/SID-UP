package unoptimized
package events
package impl

import unoptimized.signals.Signal
import unoptimized.impl.SingleDependentReactive

class DeltaEventStream[A](val dependency: Signal[A]) extends DependentEventStreamImpl[(A, A)] with SingleDependentReactive {
  private var lastValue: A = dependency.now
  protected def reevaluate(transaction: Transaction): Option[(A, A)] = {
    val oldValue = lastValue
    lastValue = dependency.pulse(transaction).get
    Some(oldValue -> lastValue)
  }
}
