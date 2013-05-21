package reactive
package events
package impl

import java.util.UUID
import util.TransactionalAccumulator
import util.TicketAccumulator

class MergeStream[A](streams: Iterable[EventStream[A]]) extends EventStreamImpl[A](streams.foldLeft(Set[UUID]()) { (set, dep) => set ++ dep.sourceDependencies }) with EventStream.Dependant[A] {
  streams.foreach { _.addDependant(None, this) }
  private val accumulator = new TransactionalAccumulator[(Boolean, Option[A], List[TicketAccumulator.Receiver])] {
    override def initialValue(transaction: Transaction) = (false, None, Nil)
    override def expectedTickCount(transaction: Transaction) = streams.count(_.isConnectedTo(transaction))
  }

  override def notify(replyChannel : TicketAccumulator.Receiver, notification: EventNotification[A]) {
    accumulator.tickAndGetIfCompleted(notification.transaction) {
      case (anyDependencyChanged, event, replyChannels) =>
        (anyDependencyChanged || notification.sourceDependenciesUpdate.changed, event.orElse(notification.maybeValue), replyChannel :: replyChannels)
    } foreach {
      case (anyDependencyChanged, event, replyChannels) =>
        val sourceDependencyUpdate = if (anyDependencyChanged) {
          _sourceDependencies.update(streams.foldLeft(Set[UUID]()) { (set, dep) => set ++ dep.sourceDependencies })
        } else {
          _sourceDependencies.noChangeUpdate
        }

        publish(new EventNotification(notification.transaction, sourceDependencyUpdate, event), replyChannels :_*)
    }
  }
}