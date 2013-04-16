package reactive
package signals
package impl

import reactive.events.EventStream
import reactive.events.EventNotification

class HoldSignal[A](override val changes: EventStream[A], initialValue: A) extends SignalImpl[A](changes.sourceDependencies, initialValue) with EventStream.Dependant[A] {
  changes.addDependant(None, this);
  override def notify(notification: EventNotification[A]) {
    val dependencyUpdate = notification.sourceDependenciesUpdate.applyTo(_sourceDependencies)
    val valueUpdate = if (notification.maybeValue.isDefined) {
      value.update(notification.maybeValue.get);
    } else {
      value.noChangeUpdate
    }
    publish(new SignalNotification(notification.transaction, dependencyUpdate, valueUpdate))
  }
}
