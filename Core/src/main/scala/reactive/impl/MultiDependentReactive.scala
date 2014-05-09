package reactive
package impl

import java.util.UUID
import com.typesafe.scalalogging.slf4j.Logging
import scala.concurrent.stm._

trait MultiDependentReactive extends Logging {
  self: DependentReactive[_] =>

  protected val dependencies: Set[Reactive.Dependency]
  atomic { tx =>
    dependencies.foreach { _.addDependant(tx, this) }
  }

  private val pendingNotifications: Ref[Int] = Ref(0)
  private val anyDependenciesChanged: Ref[Boolean] = Ref(false)
  private val anyPulse: Ref[Boolean] = Ref(false)

  override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = {
//    synchronized {
        val stillPending = pendingNotifications.transformAndGet{previous =>
          if(previous == 0) {
            dependencies.count(_.isConnectedTo(transaction)) - 1
          } else {
            previous - 1
          }
        }(transaction.stmTx)
        anyDependenciesChanged.transform(_|| sourceDependenciesChanged)(transaction.stmTx)
        anyPulse.transform(_|| pulsed)(transaction.stmTx)
        
        if (stillPending == 0) {
          logger.trace(s"$this received last remaining notification for transaction ${transaction}, starting reevaluation")
          doReevaluation(transaction, anyDependenciesChanged.swap(false)(transaction.stmTx), anyPulse.swap(false)(transaction.stmTx))
        } else if (stillPending < 0) {
          throw new IllegalStateException(s"$this received more notifications than expected for transaction ${transaction}")
        } else {
          logger.trace(s"$this received a notification for transaction ${transaction}, ${stillPending} pending")
        }
//      }
    }

  override protected def calculateSourceDependencies(tx: InTxn): Set[UUID] = {
    dependencies.foldLeft(Set[UUID]())(_ ++ _.sourceDependencies(tx));
  }
}
