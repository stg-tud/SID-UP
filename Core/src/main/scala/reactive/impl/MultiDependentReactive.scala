package reactive
package impl

import java.util.UUID
import com.typesafe.scalalogging.LazyLogging

trait MultiDependentReactive extends CounterBasedWaiting {
  self: DependentReactive[_] with ReactiveImpl[_, _] =>

  protected val dependencies: Set[Reactive[_, _]]
  dependencies.foreach { _.addDependant(null, this) }
  override def currentDependencies = dependencies

  override def ping(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = {
    val pending = synchronized {
      countPing(transaction, sourceDependenciesChanged, pulsed)
      pendingNotifications
    }
    actOnCounterState(transaction, pending)
  }

  override protected def calculateSourceDependencies(transaction: Transaction): Set[UUID] = {
    dependencies.flatMap(_.sourceDependencies(transaction))
  }
}
