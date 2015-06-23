package reactive
package impl

import java.util.UUID
import com.typesafe.scalalogging.LazyLogging

trait CounterBasedWaiting extends LazyLogging {
  self: DependentReactive[_] with ReactiveImpl[_, _] =>

  protected def currentDependencies: Set[Reactive[_, _]]

  protected var currentTransaction: Transaction = null
  protected var pendingNotifications: Int = 0
  protected var anyDependenciesChanged: Boolean = _
  protected var anyPulse: Boolean = _

  protected def countPing(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = {
    if (currentTransaction != transaction) {
      if (!hasPulsed(currentTransaction)) throw new IllegalStateException(s"Cannot process transaction ${transaction.uuid}, previous transaction ${currentTransaction.uuid} not completed yet! ($pendingNotifications notifications pending)")
      currentTransaction = transaction
      pendingNotifications = currentDependencies.count(_.isConnectedTo(transaction)) - 1
      anyDependenciesChanged = sourceDependenciesChanged
      anyPulse = pulsed
    } else {
      pendingNotifications -= 1
      anyDependenciesChanged |= sourceDependenciesChanged
      anyPulse |= pulsed
    }
  }

  protected def actOnCounterState(transaction: Transaction, pending: Int) = {
    if (pending == 0) {
      logger.trace(s"$this received last remaining notification for transaction ${transaction.uuid}, starting reevaluation")
      doReevaluation(transaction, anyDependenciesChanged, anyPulse)
    } else if (pending < 0) {
      throw new IllegalStateException(s"$this received more notifications than expected for transaction ${transaction.uuid}")
    } else {
      logger.trace(s"$this received a notification for transaction ${transaction.uuid}, $pendingNotifications pending")
    }
  }
}
