package reactive
package events
package impl

import java.util.UUID

import reactive.impl.MultiDependentReactive
import reactive.signals.Signal

import scala.concurrent.stm.InTxn

abstract class GeneralPulseEventStream(tx: InTxn) extends MultiDependentReactive(tx) {
  self: DependentEventStreamImpl[_] =>
  protected val events: EventStream[Any]

  // if the transaction does not touch the events all notifications are discarded,
  // because it is expected that this will not pulse.
  override def ping(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = {
    if (events.isConnectedTo(transaction)) {
      super.ping(transaction, sourceDependenciesChanged, pulsed)
    }
  }

  // report that this only pulses on changes of the event stream
  override protected def calculateSourceDependencies(tx: InTxn): Set[UUID] = events.transactional.sourceDependencies(tx)
}

/**
 * triggers an event with the value of `signal` every time `events` fires
 *
 * depends on the value of both the signal and the event stream, so both are dependencies.
 * but reports only the events stream as an actual dependency downstream,
 * because a change in only the value of the signal will never change the pulse of this.
 */
class PulseEventStream[A](private val signal: Signal[A], override protected val events: EventStream[_], tx: InTxn) extends {
  override val dependencies = Set[Reactive.Dependency](events, signal)
} with GeneralPulseEventStream(tx) with DependentEventStreamImpl[A] {
  override protected def reevaluate(tx: InTxn): Option[A] = {
    events.pulse(tx).asOption.map { eventVal =>
      signal.pulse(tx).asOption.getOrElse(signal.transactional.now(tx))
    }
  }
}

class TuplePulseEventStream1[X, A](override protected val events: EventStream[X], private val signal1: Signal[A], tx: InTxn) extends {
  override val dependencies = Set[Reactive.Dependency](events, signal1)
} with GeneralPulseEventStream(tx) with DependentEventStreamImpl[(X, A)] {
  override protected def reevaluate(tx: InTxn): Option[(X, A)] = {
    events.pulse(tx).asOption.map {
      (_,
        signal1.pulse(tx).asOption.getOrElse(signal1.transactional.now(tx)))
    }
  }
}

class TuplePulseEventStream2[X, A, B](override protected val events: EventStream[X], private val signal1: Signal[A], private val signal2: Signal[B], tx: InTxn) extends {
  override val dependencies = Set[Reactive.Dependency](events, signal1, signal2)
} with GeneralPulseEventStream(tx) with DependentEventStreamImpl[(X, A, B)] {
  override protected def reevaluate(tx: InTxn): Option[(X, A, B)] = {
    events.pulse(tx).asOption.map {
      (_,
        signal1.pulse(tx).asOption.getOrElse(signal1.transactional.now(tx)),
        signal2.pulse(tx).asOption.getOrElse(signal2.transactional.now(tx)))
    }
  }
}

