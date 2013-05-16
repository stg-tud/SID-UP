package reactive
package impl

import java.util.UUID
import scala.collection.mutable
import util.MutableValue
import util.TransactionalTransientVariable

abstract class ReactiveImpl[A, N <: ReactiveNotification[A]](initialSourceDependencies: Set[UUID]) extends Reactive[A, N] {
  protected val _sourceDependencies = new MutableValue(initialSourceDependencies);
  override def sourceDependencies = _sourceDependencies.current

  override def isConnectedTo(transaction: Transaction) = !(transaction.sources & sourceDependencies).isEmpty
  private var dependants = Set[ReactiveDependant[N]]()

  override def addDependant(maybeTransaction: Option[Transaction], dependant: ReactiveDependant[N]) {
    dependants += dependant
    maybeTransaction.flatMap { _lastNotification.getIfSet(_) }.foreach { dependant.notify(_) }
  }
  override def maybeNotification(transaction : Transaction) = {
    _lastNotification.getIfSet(transaction)
  }

  override def removeDependant(dependant: ReactiveDependant[N]) {
    dependants -= dependant
  }

  protected val _lastNotification = new TransactionalTransientVariable[N]
  def publish(notification: N) {
    _lastNotification.set(notification.transaction, notification)
    dependants.foreach(_.notify(notification));
  }
  // ====== Observing stuff ======

  private val observers = mutable.Set[A => Unit]()
  def observe(obs: A => Unit) {
    observers += obs
  }
  def unobserve(obs: A => Unit) {
    observers -= obs
  }

  protected def notifyObservers(value: A) {
    observers.foreach { _(value) }
  }
}