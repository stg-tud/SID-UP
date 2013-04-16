package reactive
package events
package impl

import java.util.UUID
import util.TransactionalAccumulator

class MergeStream[A](streams: Iterable[EventStream[A]]) extends EventStreamImpl[A](streams.foldLeft(Set[UUID]()) { (set, dep) => set ++ dep.sourceDependencies }) with EventStream.Dependant[A] {
  streams.foreach { _.addDependant(this) }
  private val accumulator = new TransactionalAccumulator[(Boolean, Option[A])] {
    override val initialValue = (false, None)
    override def expectedTickCount(transaction: Transaction) = streams.count(_.isConnectedTo(transaction))
  }

  override def notify(notification: EventNotification[A]) {
    accumulator.tickAndGetIfCompleted(notification.transaction) {
      case (anyDependencyChanged, event) =>
        (anyDependencyChanged || notification.sourceDependenciesUpdate.changed, event.orElse(notification.maybeValue))
    } foreach {
      case (anyDependencyChanged, event) =>
        val sourceDependencyUpdate = if (anyDependencyChanged) {
          _sourceDependencies.update(streams.foldLeft(Set[UUID]()) { (set, dep) => set ++ dep.sourceDependencies })
        } else {
          _sourceDependencies.noChangeUpdate
        }

        publish(new EventNotification(notification.transaction, sourceDependencyUpdate, event))
    }
  }
}