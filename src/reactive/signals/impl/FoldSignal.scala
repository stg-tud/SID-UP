package reactive
package signals
package impl

import reactive.events.EventStream
import reactive.events.EventNotification

class FoldSignal[A, B](initialValue: A, source: EventStream[B], op: (A, B) => A) extends SignalImpl[A](source.sourceDependencies, initialValue) with EventStream.Dependant[B] {

  override def notify(notification : EventNotification[B]) {
    val dependencyUpdate = notification.sourceDependenciesUpdate.applyTo(_sourceDependencies);
    val valueUpdate = if (notification.maybeValue.isDefined) {
      value.update(op(this()(notification.transaction), notification.maybeValue.get));
    } else {
      value.noChangeUpdate
    }
    publish(new SignalNotification(notification.transaction, dependencyUpdate, valueUpdate))
  }
}

object FoldSignal {
  def apply[A, B](initialValue: A, source: EventStream[B], op: (A, B) => A) = {

  }
}
