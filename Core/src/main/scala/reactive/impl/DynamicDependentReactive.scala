package reactive
package impl

import java.util.UUID
import com.typesafe.scalalogging.LazyLogging

trait DynamicDependentReactive extends CounterBasedWaiting {
  self: DependentReactive[_] with ReactiveImpl[_, _] =>

  protected def dependencies(transaction: Transaction): Set[Reactive[_, _]]

  private var lastDependencies = dependencies(null)
  lastDependencies.foreach { _.addDependant(null, this) }
  override def currentDependencies = lastDependencies

  override def ping(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = {
    val pending = synchronized {
      countPing(transaction, sourceDependenciesChanged, pulsed)
      if (pulsed) {
        anyDependenciesChanged |= updateDependencySubscriptions(transaction)
      }
      pendingNotifications 
    }

    actOnCounterState(transaction, pending)
  }

  private def updateDependencySubscriptions(transaction: Transaction): Boolean = {
    val newDependencies = dependencies(transaction)
    val unsubscribe = lastDependencies.diff(newDependencies)
    val subscribe = newDependencies.diff(lastDependencies)
    lastDependencies = newDependencies

    unsubscribe.foreach { dep =>
      if (dep.removeDependant(transaction, this)) pendingNotifications -= 1
    }
    subscribe.foreach { dep =>
      if (dep.addDependant(transaction, this)) pendingNotifications += 1
    }

    unsubscribe.nonEmpty | subscribe.nonEmpty
  }

  protected def calculateSourceDependencies(transaction: Transaction): Set[UUID] = {
    dependencies(transaction).flatMap(_.sourceDependencies(transaction))
  }
}
