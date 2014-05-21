package reactive
package events
package impl

import java.util.UUID
import reactive.impl.MultiDependentReactive
import reactive.signals.Signal
import scala.concurrent.stm.InTxn

/** triggers an event with the value of `signal` every time `events` fires
	*
	* depends on the value of both the signal and the event stream, so both are dependencies.
	* but reports only the events stream as an actual dependency downstream,
	* because a change in only the value of the signal will never change the pulse of this.
	*/
class PulseEventStream[A](private val signal: Signal[A], private val events: EventStream[_], tx: InTxn) extends {
  override val dependencies = Set[Reactive.Dependency](events, signal)
} with MultiDependentReactive(tx) with DependentEventStreamImpl[A] {

  override protected def reevaluate(tx: InTxn): Option[A] = {
    events.pulse(tx).asOption.map { eventVal =>
      signal.pulse(tx).asOption.getOrElse(signal.now(tx))
    }
  }

  // if the transaction does not touch the events all notifications are discarded,
  // because it is expected that this will not pulse.
  override def ping(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean) {
  	if(events.isConnectedTo(transaction)) {
  		super.ping(transaction, sourceDependenciesChanged, pulsed)
  	}
  }

  // report that this only pulses on changes of the event stream
  override protected def calculateSourceDependencies(tx: InTxn): Set[UUID] = events.sourceDependencies(tx)
}
