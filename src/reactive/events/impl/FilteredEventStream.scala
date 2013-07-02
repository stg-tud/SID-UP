package reactive
package events
package impl

import util.TicketAccumulator

class FilteredEventStream[A](from: EventStream[A], op: A => Boolean) extends EventStreamImpl[A](from.sourceDependencies) with EventStream.Dependant[A] {
  from.addDependant(None, this)
  override def notify(replyChannel : TicketAccumulator.Receiver, notification : EventStream.Notification[A]) {
    publish(new EventStream.Notification(notification.transaction, notification.sourceDependenciesUpdate.applyTo(_sourceDependencies), notification.pulse.filter(op)), replyChannel);
  }
}
