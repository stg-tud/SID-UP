package reactive
package events
package impl

import java.util.UUID
import reactive.impl.MultiDependentReactive
import reactive.signals.Signal

/**
 * triggers an event with the value of `signal` every time `events` fires
 *
 * depends on the value of both the signal and the event stream, so both are dependencies.
 * but reports only the events stream as an actual dependency downstream,
 * because a change in only the value of the signal will never change the pulse of this.
 */
class PulseEventStream[A](private val signal: Signal[A], private val events: EventStream[_]) extends {
  override val dependencies = Set[DependableReactive](events, signal)
} with DependentEventStreamImpl[A] with MultiDependentReactive[A, Reactive.IDENTITY, Reactive.UNIT, Reactive.IDENTITY, EventStream] {

  override protected def reevaluatePulse(transaction: Transaction): Option[A] = {
    events.pulse(transaction).map { eventVal =>
      signal.pulse(transaction).getOrElse(signal.value(transaction))
    }
  }

  // if the transaction does not touch the events all notifications are discarded,
  // because it is expected that this will not pulse.
  override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean) {
    if (events.isConnectedTo(transaction)) {
      super.apply(transaction, sourceDependenciesChanged, pulsed)
    }
  }

  // report that this only pulses on changes of the event stream
  override protected def calculateSourceDependencies(transaction: Transaction): Set[UUID] = events.sourceDependencies(transaction)
}
