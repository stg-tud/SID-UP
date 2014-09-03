package reactive
package impl

import java.util.UUID
import com.typesafe.scalalogging.LazyLogging

trait DynamicDependentReactive extends LazyLogging {
  self: DependentReactive[_] with ReactiveImpl[_, _] =>

  protected def dependencies(transaction: Transaction): Set[Reactive[_, _]]

  private var lastDependencies = dependencies(null)
  lastDependencies.foreach { _.addDependant(null, this) }
  private var currentTransaction: Transaction = _
  private var anyDependenciesChanged: Boolean = _
  private var anyPulse: Boolean = _

  override def ping(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = {
    val waitingFor = synchronized {
      if (currentTransaction != transaction) {
        if (!hasPulsed(currentTransaction)) throw new IllegalStateException(s"Cannot process transaction ${transaction.uuid}, Previous transaction ${currentTransaction.uuid} not completed yet!")
        currentTransaction = transaction
        anyDependenciesChanged = false
        anyPulse = false
      }

      if (hasPulsed(transaction)) {
        throw new IllegalStateException(s"Already pulsed in transaction ${transaction.uuid} but received another update")
      } else {
        anyDependenciesChanged |= sourceDependenciesChanged
        anyPulse |= pulsed

        val newDependencies = dependencies(transaction)
        val unsubscribe = lastDependencies.diff(newDependencies)
        val subscribe = newDependencies.diff(lastDependencies)
        lastDependencies = newDependencies
        unsubscribe.foreach { dep =>
          anyDependenciesChanged = true
          dep.removeDependant(transaction, this)
        }
        subscribe.foreach { dep =>
          anyDependenciesChanged = true
          dep.addDependant(transaction, this)
        }

        lastDependencies.filter(dependency => dependency.isConnectedTo(transaction) && !dependency.hasPulsed(transaction))
      }
    }
    
    if (waitingFor.isEmpty) {
      doReevaluation(transaction, anyDependenciesChanged, anyPulse)
    } else {
      logger.trace(s"$name still waits for updates from $waitingFor)")
    }
  }

  protected def calculateSourceDependencies(transaction: Transaction): Set[UUID] = {
    dependencies(transaction).flatMap(_.sourceDependencies(transaction))
  }
}
