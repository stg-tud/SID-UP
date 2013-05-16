package reactive
package signals
package impl

import reactive.events.EventStream
import reactive.events.EventNotification

class FoldSignal[A, B](initialValue: A, source: EventStream[B], op: (A, B) => A) extends SignalImpl[A](source.sourceDependencies, initialValue) with EventStream.Dependant[B] {
  source.addDependant(None, this)

  override def notify(notification: EventNotification[B]) {
    val dependencyUpdate = notification.sourceDependenciesUpdate.applyTo(_sourceDependencies);
    val valueUpdate = if (notification.maybeValue.isDefined) {
      value.transform { op(_: A, notification.maybeValue.get) };
    } else {
      value.noChangeUpdate
    }
    publish(new SignalNotification(notification.transaction, dependencyUpdate, valueUpdate))
  }
}
