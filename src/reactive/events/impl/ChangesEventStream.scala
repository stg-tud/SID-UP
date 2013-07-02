package reactive
package events
package impl

import reactive.signals.Signal
import util.TicketAccumulator

class ChangesEventStream[A](from: Signal[A]) extends EventStreamImpl[A](from.sourceDependencies) with Signal.Dependant[A] {
  from.addDependant(None, this)
  override def notify(replyChannel : TicketAccumulator.Receiver, notification: Signal.Notification[A]) {
    publish(new EventStream.Notification(notification.transaction, notification.sourceDependenciesUpdate.applyTo(_sourceDependencies), notification.pulse.newValueIfChanged), replyChannel)
  }
}