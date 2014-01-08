package reactive
package impl

import java.util.UUID

import scala.language.higherKinds
trait DynamicDependentReactive[X, +OW[+_], +VW[+_], +PW[+_], +R[+Y] <: Reactive[Y, OW, VW, PW, R]] {
  self: DependentReactive[X, OW, VW, PW, R] =>

  protected def dependencies(transaction: Transaction): Set[DependableReactive]
  private var lastDependencies = dependencies(null)
  lastDependencies.foreach { _.addDependant(null, this) }

  private var currentTransaction: Transaction = _
  private var anyDependenciesChanged: Boolean = _
  private var anyPulse: Boolean = _
  private var hasPulsed: Boolean = true

  override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean) {
    synchronized {
      if (currentTransaction != transaction) {
        if (!hasPulsed) throw new IllegalStateException(s"Cannot process transaction ${transaction.uuid}, Previous transaction ${currentTransaction.uuid} not completed yet!")
        currentTransaction = transaction
        anyDependenciesChanged = false;
        anyPulse = false;
        hasPulsed = false
      }
      
      if (!hasPulsed) {
        val newDependencies = dependencies(transaction)
        val unsubscribe = lastDependencies.diff(newDependencies)
        val subscribe = newDependencies.diff(lastDependencies)

        lastDependencies = newDependencies
        anyDependenciesChanged |= sourceDependenciesChanged
        anyPulse |= pulsed;
        unsubscribe.foreach {
          anyDependenciesChanged |= true;
          anyPulse |= true;
          _.removeDependant(transaction, this)
        }
        subscribe.foreach {
          anyDependenciesChanged |= true;
          anyPulse |= true;
          _.addDependant(transaction, this)
        }

        if (!lastDependencies.exists { dependency => dependency.isConnectedTo(transaction) && !dependency.hasPulsed(transaction) }) {
          hasPulsed = true
          doReevaluation(transaction, anyDependenciesChanged, anyPulse)
        }
      }
    }
  }

  protected def calculateSourceDependencies(transaction: Transaction): Set[UUID] = {
    dependencies(transaction).flatMap(_.sourceDependencies(transaction));
  }
}
