package reactive
package impl

import java.util.UUID

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.stm._

abstract class MultiDependentReactive(constructionTransaction: InTxn) extends LazyLogging {
  self: DependentReactive[_] =>

  protected val dependencies: Set[Reactive.Dependency]
  dependencies.foreach { _.addDependant(constructionTransaction, this) }

  private val pendingNotifications: TxnLocal[Int] = TxnLocal(-1)
  private val anyDependenciesChanged: TxnLocal[Boolean] = TxnLocal(false)
  private val anyPulse: TxnLocal[Boolean] = TxnLocal(false)

  private def updateAndGetStillPending(transaction: Transaction) = {
    pendingNotifications.transformAndGet { previous =>
      if (previous == -1) {
        dependencies.count(_.isConnectedTo(transaction)) - 1
      }
      else {
        previous - 1
      }
    }(transaction.stmTx)
  }

  private def checkIfDependenciesPulsed(transaction: Transaction) = {
    val stillPending = updateAndGetStillPending(transaction)
    if (stillPending == 0) {
      logger.trace(s"$this received last remaining notification for transaction $transaction, starting reevaluation")
      true
    } else if (stillPending < 0) {
      logger.error(s"$this received orphaned notification after having pulsed; ignoring notification")
      false
    } else {
      logger.trace(s"$this received a notification for transaction $transaction, $stillPending pending")
      false
    }
  }

  override def ping(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = {
    val tx = transaction.stmTx
    val pulseNow = tx.synchronized {
      if (sourceDependenciesChanged) anyDependenciesChanged.set(true)(tx)
      if (pulsed) anyPulse.set(true)(tx)
      checkIfDependenciesPulsed(transaction)
    }
    if (pulseNow) {
      val (dependenciesChangedFlag, pulsedFlag) = tx.synchronized {
        (anyDependenciesChanged.get(tx), anyPulse.get(tx))
      }
      doReevaluation(transaction, dependenciesChangedFlag, pulsedFlag)
    }
  }

  override protected def calculateSourceDependencies(tx: InTxn): Set[UUID] = {
    dependencies.foldLeft(Set[UUID]())(_ ++ _.sourceDependencies(tx))
  }
}
