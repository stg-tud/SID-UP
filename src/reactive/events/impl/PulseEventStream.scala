package reactive
package events
package impl

import reactive.signals.Signal
import reactive.impl.MultiDependentReactive

class PulseEventStream[A](private val signal: Signal[A], private val events: EventStream[_]) extends {
  override val dependencies = Set(signal, events)
} with EventStreamImpl[A] with MultiDependentReactive[A] {
  protected def calculatePulse(transaction: Transaction): Option[A] = {
    events.pulse(transaction).map { _ =>
      signal.pulse(transaction).getOrElse(signal.value(transaction))
    }
  }
}