package reactive
package impl

import java.util.UUID
import com.typesafe.scalalogging.slf4j.Logging
import scala.concurrent.stm._

trait DynamicDependentReactive extends Logging {
  self: DependentReactive[_] with ReactiveImpl[_, _] =>

  protected def dependencies(tx: InTxn): Set[Reactive[_, _]]

  private val lastDependencies = atomic { tx =>
    val depts = dependencies(tx)
    depts.foreach { _.addDependant(tx, this) }
    Ref(depts)
  }
  private val anyDependenciesChanged: Ref[Boolean] = Ref(false)
  private val anyPulse: Ref[Boolean] = Ref(false)

  override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = {
    val tx = transaction.stmTx
    if ( //synchronized {
    if (hasPulsed(tx)) {
      //        throw new IllegalStateException(s"Already pulsed in transaction ${transaction.uuid} but received another update")
      false
    } else {
      anyPulse.transform(_ || pulsed)(tx)
      anyDependenciesChanged.transform(_ || sourceDependenciesChanged)(tx)
      val newDependencies = if (pulsed) {
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
        newDependencies
      } else {
        lastDependencies()(tx)
      }

      //if (newDependencies.exists { dependency => dependency.isConnectedTo(transaction) && !dependency.hasPulsed(transaction) }) {
      val waitingFor = newDependencies.filter(dependency => dependency.isConnectedTo(transaction) && !dependency.hasPulsed(tx))
      if (waitingFor.isEmpty) {
        true
      } else {
        logger.trace(s"$name still waits for updates from $waitingFor)")
        false
      }
    } /*}*/ ) {
      doReevaluation(transaction, anyDependenciesChanged.swap(false)(tx), anyPulse.swap(false)(tx))
    }
  }

  protected def calculateSourceDependencies(tx: InTxn): Set[UUID] = {
    dependencies(tx).flatMap(_.sourceDependencies(tx))
  }
}
