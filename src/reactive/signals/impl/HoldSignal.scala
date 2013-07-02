package reactive
package signals
package impl

import reactive.events.EventStream
import util.TicketAccumulator

class HoldSignal[A](stream: EventStream[A], initialValue: A) extends SignalImpl[A](stream.sourceDependencies, initialValue) with EventStream.Dependant[A] {
  stream.addDependant(None, this);
  override lazy val changes = stream
  override def notify(replyChannel : TicketAccumulator.Receiver, notification: EventStream.Notification[A]) {
    val dependencyUpdate = notification.sourceDependenciesUpdate.applyTo(_sourceDependencies)
    val valueUpdate = if (notification.pulse.isDefined) {
      value.update(notification.pulse.get);
    } else {
      value.noChangeUpdate
    }
    publish(new Signal.Notification(notification.transaction, dependencyUpdate, valueUpdate), replyChannel)
  }
}
