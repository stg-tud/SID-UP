package reactive
package impl

import java.util.UUID
import scala.concurrent.stm._
import com.typesafe.scalalogging.LazyLogging

abstract class DynamicDependentReactive(constructionTransaction: InTxn) extends LazyLogging {
  self: DependentReactive[_] =>

  protected def dependencies(tx: InTxn): Set[Reactive[_, _]]

  private val lastDependencies = {
    val depts = dependencies(constructionTransaction)
    depts.foreach { _.addDependant(constructionTransaction, this) }
    Ref(depts)
  }

  private val anyDependenciesChanged: TxnLocal[Boolean] = TxnLocal(false)
  private val anyPulse: TxnLocal[Boolean] = TxnLocal(false)
  private val hasPulsedLocal: TxnLocal[Boolean] = TxnLocal(false)

  /**
   * recalculates the current dependencies,
   * updates the cached dependencies
   * and handles registrations and unregistrations of changed dependencies
   */
  private def handleDependencyChanges(tx: InTxn) = {
    val newDependencies = dependencies(tx)
    val oldDependencies = lastDependencies.swap(newDependencies)(tx)
    val unsubscribe = oldDependencies.diff(newDependencies)
    val subscribe = newDependencies.diff(oldDependencies)
    unsubscribe.foreach { dep =>
      dep.removeDependant(tx, this)
    }
    subscribe.foreach { dep =>
      dep.addDependant(tx, this)
    }
    if (!unsubscribe.isEmpty || !subscribe.isEmpty) {
      anyDependenciesChanged.set(true)(tx)
    }
  }

  /**
   * @return true iff all dependencies have pulsed and this reactive has not yet pulsed
   */
  private def checkIfDependenciesPulsed(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean) = {
    val tx = transaction.stmTx
    tx.synchronized {
      if (hasPulsedLocal()(tx)) {
        logger.trace(s"$this received orphaned notification after having pulsed; ignoring notification")
        false
      }
      else {
        if (sourceDependenciesChanged) anyDependenciesChanged.set(true)(tx)
        if (pulsed) {
          anyPulse.set(true)(tx)
          handleDependencyChanges(tx)
        }

        val waitingFor = lastDependencies()(tx).filter(dependency => dependency.isConnectedTo(transaction) && !dependency.hasPulsed(tx))
        if (waitingFor.isEmpty) {
          hasPulsedLocal.set(true)(tx)
          true
        }
        else {
          logger.trace(s"$this still waits for updates from $waitingFor)")
          false
        }
      }
    }
  }

  override def ping(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = {
    val tx = transaction.stmTx
    val pulseNow = checkIfDependenciesPulsed(transaction, sourceDependenciesChanged, pulsed)
    if (pulseNow) {
      val (dependenciesChangedFlag, pulsedFlag) = tx.synchronized {
        (anyDependenciesChanged.get(tx), anyPulse.get(tx))
      }
      doReevaluation(transaction, dependenciesChangedFlag, pulsedFlag)
    }
  }

  protected def calculateSourceDependencies(tx: InTxn): Set[UUID] = {
    dependencies(tx).flatMap(_.sourceDependencies(tx))
  }
}
