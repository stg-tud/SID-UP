package reactive
package impl

import java.util.UUID
import com.typesafe.scalalogging.slf4j.Logging
import scala.concurrent.stm.Ref
import scala.concurrent.stm.atomic
import scala.concurrent.stm.Txn

trait DynamicDependentReactive extends Logging {
  self: DependentReactive[_] with ReactiveImpl[_, _] =>

  protected def dependencies(transaction: Transaction): Set[Reactive[_, _]]

  private val lastDependencies = Ref(dependencies(null))
  atomic { tx => lastDependencies()(tx).foreach { _.addDependant(null, this) } }
  private val anyDependenciesChanged: Ref[Boolean] = Ref(false)
  private val anyPulse: Ref[Boolean] = Ref(false)

  override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = atomic { implicit tx =>
    if (//synchronized {
      if (hasPulsed(transaction)) {
        throw new IllegalStateException(s"Already pulsed in transaction ${transaction.uuid} but received another update")
      } else {
        anyPulse.transform(_ || pulsed)
        anyDependenciesChanged.transform(_ || sourceDependenciesChanged)
        if (pulsed) {
          val newDependencies = dependencies(transaction)
          val unsubscribe = lastDependencies().diff(newDependencies)
          val subscribe = newDependencies.diff(lastDependencies())
          lastDependencies() = newDependencies
          unsubscribe.foreach { dep =>
            dep.removeDependant(transaction, this)
          }
          subscribe.foreach { dep =>
            dep.addDependant(transaction, this)
          }
          if (!unsubscribe.isEmpty || !subscribe.isEmpty) {
            anyDependenciesChanged() = true
          }
        }

        val waitingFor = lastDependencies().filter(dependency => dependency.isConnectedTo(transaction) && !dependency.hasPulsed(transaction))

        //if (!lastDependencies().exists { dependency => dependency.isConnectedTo(transaction) && !dependency.hasPulsed(transaction) }) {
        if (waitingFor.isEmpty) {
          true
        } else {
          logger.trace(s"$name still waits for updates from $waitingFor)")
          false
        }
      }
    /*}*/) {
      doReevaluation(transaction, anyDependenciesChanged(), anyPulse())
      anyDependenciesChanged() = false
      anyPulse() = false
    }
  }

  protected def calculateSourceDependencies(transaction: Transaction): Set[UUID] = {
    dependencies(transaction).flatMap(_.sourceDependencies(transaction))
  }
}
