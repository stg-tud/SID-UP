package reactive
package impl

import java.util.UUID
import com.typesafe.scalalogging.slf4j.Logging
import scala.concurrent.stm.Ref
import scala.concurrent.stm.atomic

trait DynamicDependentReactive extends Logging {
  self: DependentReactive[_] with ReactiveImpl[_, _] =>

  protected def dependencies(transaction: Transaction): Set[Reactive[_, _]]

  private var lastDependencies = Ref(dependencies(null))
  atomic { tx => lastDependencies()(tx).foreach { _.addDependant(null, this) } }
  private var currentTransaction: Ref[Transaction] = Ref(null)
  private var anyDependenciesChanged: Ref[Boolean] = Ref(false)
  private var anyPulse: Ref[Boolean] = Ref(false)

  override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = atomic { implicit tx =>
    if (synchronized {
      if (currentTransaction() != transaction) {
        if (!hasPulsed(currentTransaction())) throw new IllegalStateException(s"Cannot process transaction ${transaction.uuid }, Previous transaction ${currentTransaction().uuid } not completed yet!")
        currentTransaction() = transaction
        anyDependenciesChanged() = false
        anyPulse() = false
      }

      if (hasPulsed(transaction)) {
        throw new IllegalStateException(s"Already pulsed in transaction ${transaction.uuid } but received another update")
        false
      }
      else {
        val newDependencies = dependencies(transaction)
        val unsubscribe = lastDependencies().diff(newDependencies)
        val subscribe = newDependencies.diff(lastDependencies())

        lastDependencies() = newDependencies
        anyDependenciesChanged() = anyDependenciesChanged() | sourceDependenciesChanged
        anyPulse() = anyPulse() | pulsed
        unsubscribe.foreach { dep =>
          anyDependenciesChanged() = true
          anyPulse() = true
          dep.removeDependant(transaction, this)
        }
        subscribe.foreach { dep =>
          anyDependenciesChanged() = true
          anyPulse() = true
          dep.addDependant(transaction, this)
        }

        val waitingFor = lastDependencies().filter(dependency => dependency.isConnectedTo(transaction) && !dependency.hasPulsed(transaction))

        //if (!lastDependencies.exists { dependency => dependency.isConnectedTo(transaction) && !dependency.hasPulsed(transaction) }) {
        if (waitingFor.isEmpty) {
          true
        }
        else {
          logger.trace(s"$name still waits for updates from $waitingFor)")
          false
        }
      }

    }) {
      doReevaluation(transaction, anyDependenciesChanged(), anyPulse())
    }
  }

  protected def calculateSourceDependencies(transaction: Transaction): Set[UUID] = {
    dependencies(transaction).flatMap(_.sourceDependencies(transaction))
  }
}
