package reactive
package signals
package impl

import scala.collection.mutable
import reactive.events.EventStream
import util.TransactionalAccumulator
import util.TicketAccumulator
import util.Update

class SnapshotSignal[A](signal: Signal[A], events: EventStream[_]) extends SignalImpl[A](events.sourceDependencies, signal.now) with ReactiveDependant[Any] {
  private val deps = Iterable(signal, events)
  deps.foreach { _.addDependant(None, this) }
  
  private val accumulator = new TransactionalAccumulator[(Boolean, List[TicketAccumulator.Receiver])] {
    override def expectedTickCount(transaction: Transaction) = deps.count(_.isConnectedTo(transaction))
    override def initialValue(transaction: Transaction) = (false, Nil)
  }

  override def notify(replyChannel : TicketAccumulator.Receiver, notification: ReactiveNotification[Any]) {
    accumulator.tickAndGetIfCompleted(notification.transaction) { case (anyDependencyChanged, replyChannels) =>
      (anyDependencyChanged || notification.sourceDependenciesUpdate.changed, replyChannel :: replyChannels)
    } foreach { case (anyDependencyChange, replyChannels) =>
      val sourceDependencyUpdate = if (anyDependencyChange) {
        _sourceDependencies.update(signal.sourceDependencies ++ events.sourceDependencies)
      } else {
        _sourceDependencies.noChangeUpdate
      }

      val valueUpdate = if (events.isConnectedTo(notification.transaction) && events.transientPulse(notification.transaction).isDefined) {
        value.update(signal()(notification.transaction))
      } else {
        value.noChangeUpdate
      }

      publish(new ReactiveNotification[Update[A]](notification.transaction, sourceDependencyUpdate, valueUpdate), replyChannels :_*)
    }
  }
}