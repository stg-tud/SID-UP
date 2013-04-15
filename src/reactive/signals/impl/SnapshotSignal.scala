package reactive
package signals
package impl

import scala.collection.mutable
import reactive.events.EventStream

class SnapshotSignal[A](signal: Signal[A], events: EventStream[_]) extends SignalImpl[A](events.sourceDependencies, signal.now) with ReactiveDependant[ReactiveNotification[Any]] {
  private var pending = 0;
  private var anyDependencyChange = false
  private var eventFired = false

  override def notify(notification: ReactiveNotification[Any]) {
    if (pending == 0) {
      pending = Iterable(signal, events).count(_.isConnectedTo(notification.transaction))
      anyDependencyChange = false
    }

    anyDependencyChange |= notification.sourceDependenciesUpdate.changed
    pending -= 1;

    if (pending == 0) {
      val sourceDependencyUpdate = if (anyDependencyChange) {
        _sourceDependencies.update(signal.sourceDependencies ++ events.sourceDependencies)
      } else {
        _sourceDependencies.noChangeUpdate
      }

      val valueUpdate = if (events.isConnectedTo(notification.transaction) && events()(notification.transaction).isDefined) {
        value.update(signal()(notification.transaction))
      } else {
        value.noChangeUpdate
      }

      publish(new SignalNotification(notification.transaction, sourceDependencyUpdate, valueUpdate))
    }
  }
}