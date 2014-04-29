package reactive
package impl

import java.util.UUID
import com.typesafe.scalalogging.slf4j.Logging
import scala.concurrent.stm.Ref
import scala.concurrent.stm.atomic

trait MultiDependentReactive extends Logging {
  self: DependentReactive[_] =>

  protected val dependencies: Set[Reactive.Dependency]
  dependencies.foreach { _.addDependant(null, this) }

  private val currentTransaction: Ref[Transaction] = Ref(null)
  private val pendingNotifications: Ref[Int] = Ref(0)
  private val anyDependenciesChanged: Ref[Boolean] = Ref(false)
  private val anyPulse: Ref[Boolean] = Ref(false)

  override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit =
    synchronized {
      atomic { implicit tx =>
        if (currentTransaction() != transaction) {
          if (pendingNotifications() != 0) throw new IllegalStateException(s"Cannot process transaction ${transaction.uuid }, previous transaction ${currentTransaction().uuid } not completed yet! ($pendingNotifications notifications pending)")
          currentTransaction() = transaction
          pendingNotifications() = dependencies.count(_.isConnectedTo(transaction)) - 1
          anyDependenciesChanged() = sourceDependenciesChanged
          anyPulse() = pulsed
        }
        else {
          pendingNotifications() = pendingNotifications() - 1
          anyDependenciesChanged() = anyDependenciesChanged() | sourceDependenciesChanged
          anyPulse() = anyPulse() | pulsed
        }
        if (pendingNotifications() == 0) {
          logger.trace(s"$this received last remaining notification for transaction ${transaction.uuid }, starting reevaluation")
          doReevaluation(transaction, anyDependenciesChanged(), anyPulse())
        }
        else if (pendingNotifications() < 0) {
          throw new IllegalStateException(s"$this received more notifications than expected for transaction ${transaction.uuid }")
        }
        else {
          logger.trace(s"$this received a notification for transaction ${transaction.uuid }, ${pendingNotifications()} pending")
        }
      }
    }

  override protected def calculateSourceDependencies(transaction: Transaction): Set[UUID] = {
    dependencies.foldLeft(Set[UUID]())(_ ++ _.sourceDependencies(transaction));
  }
}
