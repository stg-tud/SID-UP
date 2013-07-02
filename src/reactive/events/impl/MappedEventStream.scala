package reactive
package events
package impl

import util.TicketAccumulator

class MappedEventStream[A, B](private val from: EventStream[B], private val op: B => A) extends EventStreamImpl[A](from.sourceDependencies) with EventStream.Dependant[B] {
  from.addDependant(None, this)
  override def notify(replyChannel : TicketAccumulator.Receiver, notification : EventStream.Notification[B]) {
    publish(new EventStream.Notification(notification.transaction, notification.sourceDependenciesUpdate.applyTo(_sourceDependencies), notification.pulse.map(op)), replyChannel);
  }
}
