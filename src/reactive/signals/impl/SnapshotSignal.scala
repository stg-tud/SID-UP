package reactive
package signals
package impl

import scala.collection.mutable
import reactive.events.EventStream
import util.TransactionalAccumulator

class SnapshotSignal[A](signal: Signal[A], events: EventStream[_]) extends SignalImpl[A](events.sourceDependencies, signal.now) with ReactiveDependant[ReactiveNotification[Any]] {
  private val deps = Iterable(signal, events)
  deps.foreach { _.addDependant(this) }
  
  private val accumulator = new TransactionalAccumulator[Boolean] {
    override def expectedTickCount(transaction: Transaction) = deps.count(_.isConnectedTo(transaction))
    override val initialValue = false
  }

  override def notify(notification: ReactiveNotification[Any]) {
    accumulator.tickAndGetIfCompleted(notification.transaction) {
      _ || notification.sourceDependenciesUpdate.changed
    } foreach { anyDependencyChange =>
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