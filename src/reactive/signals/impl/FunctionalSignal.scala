package reactive
package signals
package impl

import scala.collection.mutable
import java.util.UUID
import util.Util
import util.TransactionalAccumulator
import util.TicketAccumulator
import reactive.impl.MultiDependentReactive

class FunctionalSignal[A](private val op: Transaction => A, private val inputs: Signal[_]*) extends {
  override val dependencies = inputs.toSet[Reactive[_, _, _]]
} with DependentSignalImpl[A] with MultiDependentReactive[A] {
  override def reevaluate(transaction: Transaction) = op(transaction)

  //  private val accumulator = new TransactionalAccumulator[(Boolean, Boolean, List[TicketAccumulator.Receiver])] {
  //    override def expectedTickCount(transaction: Transaction) = dependencies.count(_.isConnectedTo(transaction))
  //    override def initialValue(transaction: Transaction) = (false, false, Nil)
  //  }
  //
  //  override def notify(replyChannel: TicketAccumulator.Receiver, notification: Signal.Notification[Any]) {
  //    accumulator.tickAndGetIfCompleted(notification.transaction) {
  //      case (anyDependencyChange, anyValueChange, replyChannels) =>
  //        (anyDependencyChange || notification.sourceDependenciesUpdate.changed, anyValueChange || notification.pulse.changed, replyChannel :: replyChannels)
  //    } foreach {
  //      case (anyDependencyChange, anyValueChange, replyChannels) =>
  //        val sourceDependencyUpdate = if (anyDependencyChange) {
  //          _sourceDependencies.update(dependencies.foldLeft(Set[UUID]()) { (set, dep) => set ++ dep.sourceDependencies })
  //        } else {
  //          _sourceDependencies.noChangeUpdate
  //        }
  //
  //        val valueUpdate = if (anyValueChange) {
  //          value.update(op(notification.transaction))
  //        } else {
  //          value.noChangeUpdate
  //        }
  //
  //        publish(new Signal.Notification(notification.transaction, sourceDependencyUpdate, valueUpdate), replyChannels: _*)
  //    }
  //  }
}
