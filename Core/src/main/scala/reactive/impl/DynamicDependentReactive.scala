package reactive
package impl

import java.util.UUID
import com.typesafe.scalalogging.slf4j.Logging
import scala.concurrent.stm._

abstract class DynamicDependentReactive(tx: InTxn) extends Logging {
  self: DependentReactive[_] with ReactiveImpl[_, _] =>

  protected def dependencies(tx: InTxn): Set[Reactive[_, _]]

  private val lastDependencies = {
    val depts = dependencies(tx)
    depts.foreach { _.addDependant(tx, this) }
    Ref(depts)
  }

  private val anyDependenciesChanged: Ref[Boolean] = Ref(false)
  private val anyPulse: Ref[Boolean] = Ref(false)
  private val hasPulsedLocal: Ref[Boolean] = Ref(false)

  override def ping(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = {
    val tx = transaction.stmTx
    val pulseNow = tx.synchronized {
      if (hasPulsedLocal()(tx)) {
        logger.trace(s"$name received orphaned notification after having pulsed; ignoring notification")
        false
      } else {
        anyPulse.transform(_ || pulsed)(tx)
        anyDependenciesChanged.transform(_ || sourceDependenciesChanged)(tx)

        if (pulsed) {
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
        
        val waitingFor = lastDependencies()(tx).filter(dependency => dependency.isConnectedTo(transaction) && !dependency.hasPulsed(tx))
        if (waitingFor.isEmpty) {
          Txn.beforeCommit(hasPulsedLocal.set(false)(_))(tx)
          hasPulsedLocal.set(true)(tx)
          true
        } else {
          logger.trace(s"$name still waits for updates from $waitingFor)")
          false
        }
      }
    }

    if (pulseNow) {
      val dependenciesChangedFlag = tx.synchronized(anyDependenciesChanged.swap(false)(tx))
      val pulsedFlag = tx.synchronized(anyPulse.swap(false)(tx))
      doReevaluation(transaction, dependenciesChangedFlag, pulsedFlag)
    }
  }

  protected def calculateSourceDependencies(tx: InTxn): Set[UUID] = {
    dependencies(tx).flatMap(_.sourceDependencies(tx))
  }
}
