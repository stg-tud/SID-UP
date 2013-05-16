package reactive
package signals
package impl

import scala.collection.mutable
import java.util.UUID
import util.Util
import util.TransactionalAccumulator

class FunctionalSignal[A](op: Transaction => A, dependencies: Signal[_]*) extends SignalImpl[A](dependencies.foldLeft(Set[UUID]()) { (set, dep) => set ++ dep.sourceDependencies }, op(null)) with Signal.Dependant[Any] {
  dependencies.foreach { _.addDependant(None, this) }
  private val accumulator = new TransactionalAccumulator[(Boolean, Boolean)] {
    override def expectedTickCount(transaction: Transaction) = dependencies.count(_.isConnectedTo(transaction))
    override def initialValue(transaction: Transaction) = (false, false)
  }

  override def notify(notification: SignalNotification[Any]) {
    accumulator.tickAndGetIfCompleted(notification.transaction) {
      case (anyDependencyChange, anyValueChange) =>
        (anyDependencyChange || notification.sourceDependenciesUpdate.changed, anyValueChange || notification.valueUpdate.changed)
    } foreach {
      case (anyDependencyChange, anyValueChange) =>
        val sourceDependencyUpdate = if (anyDependencyChange) {
          _sourceDependencies.update(dependencies.foldLeft(Set[UUID]()) { (set, dep) => set ++ dep.sourceDependencies })
        } else {
          _sourceDependencies.noChangeUpdate
        }

        val valueUpdate = if (anyValueChange) {
          value.update(op(notification.transaction))
        } else {
          value.noChangeUpdate
        }

        publish(new SignalNotification(notification.transaction, sourceDependencyUpdate, valueUpdate))
    }
  }
}
