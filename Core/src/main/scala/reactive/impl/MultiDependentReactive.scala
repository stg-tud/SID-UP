package reactive
package impl

import java.util.UUID
import com.typesafe.scalalogging.slf4j.Logging


trait MultiDependentReactive extends Logging {
  self: DependentReactive[_, _] =>

  protected val dependencies: Set[Reactive[_, _, _]]
  dependencies.foreach { _.addDependant(null, this) }

  private var currentTransaction: Transaction = _
  private var pendingNotifications: Int = 0
  private var anyDependenciesChanged: Boolean = _
  private var anyPulse: Boolean = _

  override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean) {
    if (currentTransaction == null || currentTransaction != transaction) {
      if (pendingNotifications != 0) throw new IllegalStateException(s"Previous transaction $transaction not completed yet! ($pendingNotifications notifications pending)")
      currentTransaction = transaction
      pendingNotifications = dependencies.count(_.isConnectedTo(transaction)) - 1
      anyDependenciesChanged = sourceDependenciesChanged
      anyPulse = pulsed;
    } else {
      pendingNotifications -= 1;
      anyDependenciesChanged |= sourceDependenciesChanged
      anyPulse |= pulsed;
    }
    logger.trace(s"$this received a notification,  $pendingNotifications remaining")
    if (pendingNotifications == 0) {
      doReevaluation(transaction, anyDependenciesChanged, anyPulse)
    } else if (pendingNotifications < 0) {
      throw new IllegalStateException(s"received more notifications than expected")
    }
  }

  override protected def calculateSourceDependencies(transaction: Transaction): Set[UUID] = {
    dependencies.foldLeft(Set[UUID]())(_ ++ _.sourceDependencies(transaction));
  }
}
