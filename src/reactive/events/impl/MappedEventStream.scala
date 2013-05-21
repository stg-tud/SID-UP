package reactive
package events
package impl

import util.TicketAccumulator

class MappedEventStream[A, B](private val from: EventStream[B], private val op: B => A) extends EventStreamImpl[A](from.sourceDependencies) with EventStream.Dependant[B] {
  from.addDependant(None, this)
  override def notify(replyChannel : TicketAccumulator.Receiver, notification : EventNotification[B]) {
    publish(new EventNotification(notification.transaction, notification.sourceDependenciesUpdate.applyTo(_sourceDependencies), notification.maybeValue.map(op)), replyChannel);
  }
}
