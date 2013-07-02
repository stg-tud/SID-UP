package reactive
package signals
package impl

import reactive.events.EventStream
import util.TicketAccumulator

class FoldSignal[A, B](initialValue: A, source: EventStream[B], op: (A, B) => A) extends SignalImpl[A](source.sourceDependencies, initialValue) with EventStream.Dependant[B] {
  source.addDependant(None, this)

  override def notify(replyChannel : TicketAccumulator.Receiver, notification: EventStream.Notification[B]) {
    val dependencyUpdate = notification.sourceDependenciesUpdate.applyTo(_sourceDependencies);
    val valueUpdate = if (notification.pulse.isDefined) {
      value.transform { op(_: A, notification.pulse.get) };
    } else {
      value.noChangeUpdate
    }
    publish(new Signal.Notification(notification.transaction, dependencyUpdate, valueUpdate), replyChannel)
  }
}
