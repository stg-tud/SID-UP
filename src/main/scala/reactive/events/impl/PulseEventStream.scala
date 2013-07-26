package reactive
package events
package impl

import reactive.signals.Signal
import reactive.impl.MultiDependentReactive

class PulseEventStream[A](private val signal: Signal[A], private val events: EventStream[_]) extends {
  override val dependencies = Set(signal, events)
} with DependentEventStreamImpl[A] with MultiDependentReactive {
  protected def reevaluatePulse(transaction: Transaction): Option[A] = {
    events.pulse(transaction).map { _ =>
      signal.pulse(transaction).getOrElse(signal.value(transaction))
    }
  }
}