package reactive
package events
package impl

import scala.collection.mutable
import reactive.events.EventStream
import util.TransactionalAccumulator
import util.TicketAccumulator
import util.Update
import reactive.signals.Signal

class PulseEventStream[A](signal: Signal[A], events: EventStream[_]) extends EventStreamImpl[A](signal.sourceDependencies ++ events.sourceDependencies) with ReactiveDependant[Any] {
  private val deps = Iterable(signal, events)
  deps.foreach { _.addDependant(None, this) }

  private val accumulator = new TransactionalAccumulator[(Boolean, List[TicketAccumulator.Receiver])] {
    override def expectedTickCount(transaction: Transaction) = deps.count(_.isConnectedTo(transaction))
    override def initialValue(transaction: Transaction) = (false, Nil)
  }

  override def notify(replyChannel: TicketAccumulator.Receiver, notification: ReactiveNotification[Any]) {
    accumulator.tickAndGetIfCompleted(notification.transaction) {
      case (anyDependencyChanged, replyChannels) =>
        (anyDependencyChanged || notification.sourceDependenciesUpdate.changed, replyChannel :: replyChannels)
    } foreach {
      case (anyDependencyChange, replyChannels) =>
        val sourceDependencyUpdate = if (anyDependencyChange) {
          _sourceDependencies.update(signal.sourceDependencies ++ events.sourceDependencies)
        } else {
          _sourceDependencies.noChangeUpdate
        }

        val valueUpdate = if (events.isConnectedTo(notification.transaction) && events.transientPulse(notification.transaction).isDefined) {
          Some(signal()(notification.transaction))
        } else {
          None
        }

        publish(new EventStream.Notification(notification.transaction, sourceDependencyUpdate, valueUpdate), replyChannels: _*)
    }
  }
}