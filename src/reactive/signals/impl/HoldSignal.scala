package reactive
package signals
package impl

import reactive.events.EventStream
import reactive.events.EventNotification
import util.TicketAccumulator

class HoldSignal[A](stream: EventStream[A], initialValue: A) extends SignalImpl[A](stream.sourceDependencies, initialValue) with EventStream.Dependant[A] {
  stream.addDependant(None, this);
  override lazy val changes = stream
  override def notify(replyChannel : TicketAccumulator.Receiver, notification: EventNotification[A]) {
    val dependencyUpdate = notification.sourceDependenciesUpdate.applyTo(_sourceDependencies)
    val valueUpdate = if (notification.maybeValue.isDefined) {
      value.update(notification.maybeValue.get);
    } else {
      value.noChangeUpdate
    }
    publish(new SignalNotification(notification.transaction, dependencyUpdate, valueUpdate), replyChannel)
  }
}
