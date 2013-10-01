package reactive
package impl

import java.util.UUID
import com.typesafe.scalalogging.slf4j.Logging

trait MultiDependentReactive extends Logging {
  self: DependentReactive[_, _] =>

  protected val dependencies: Set[Reactive.Dependency]
  dependencies.foreach { _.addDependant(null, this) }

  private var currentTransaction: Transaction = null
  private var pendingNotifications: Int = 0
  private var anyDependenciesChanged: Boolean = _
  private var anyPulse: Boolean = _

  override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean) {
    synchronized {
      if (currentTransaction != transaction) {
        if (pendingNotifications != 0) throw new IllegalStateException(s"Cannot process transaction ${transaction.uuid}, previous transaction ${currentTransaction.uuid} not completed yet! ($pendingNotifications notifications pending)")
        currentTransaction = transaction
        pendingNotifications = dependencies.count(_.isConnectedTo(transaction)) - 1
        anyDependenciesChanged = sourceDependenciesChanged
        anyPulse = pulsed;
      } else {
        pendingNotifications -= 1;
        anyDependenciesChanged |= sourceDependenciesChanged
        anyPulse |= pulsed;
      }
      if (pendingNotifications == 0) {
        logger.trace(s"$this received last remaining notification for transaction ${transaction.uuid}, starting reevaluation")
        doReevaluation(transaction, anyDependenciesChanged, anyPulse)
      } else if (pendingNotifications < 0) {
        throw new IllegalStateException(s"$this received more notifications than expected for transaction ${transaction.uuid}")
      } else {
        logger.trace(s"$this received a notification for transaction ${transaction.uuid}, $pendingNotifications pending")
      }
    }
  }

  override protected def calculateSourceDependencies(transaction: Transaction): Set[UUID] = {
    dependencies.foldLeft(Set[UUID]())(_ ++ _.sourceDependencies(transaction));
  }
}
