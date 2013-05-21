package reactive
package signals
package impl

import reactive.events.EventStream
import util.Util
import util.TicketAccumulator

class MapSignal[A, B](from: Signal[B], op: B => A) extends SignalImpl[A](from.sourceDependencies, op(from.now)) with Signal.Dependant[B] {
  from.addDependant(None, this)
  override def notify(replyChannel : TicketAccumulator.Receiver, notification: SignalNotification[B]) {
    val dependencyUpdate = notification.sourceDependenciesUpdate.applyTo(_sourceDependencies)
    val newValue = notification.valueUpdate.applyToMapped(value, op)
    publish(new SignalNotification(notification.transaction, dependencyUpdate, newValue), replyChannel);
  }
}