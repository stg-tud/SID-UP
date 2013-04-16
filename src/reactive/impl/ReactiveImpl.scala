package reactive
package impl

import java.util.UUID
import scala.collection.mutable
import util.MutableValue
import util.TransactionalTransientVariable

abstract class ReactiveImpl[A, N <: ReactiveNotification[A]](initialSourceDependencies : Set[UUID]) extends Reactive[A, N] {
  protected val _sourceDependencies = new MutableValue(initialSourceDependencies);
  override def sourceDependencies = _sourceDependencies.current
  
  override def isConnectedTo(transaction : Transaction) = ! (transaction.sources & sourceDependencies).isEmpty
  private var dependants = Set[ReactiveDependant[N]]()

  override def addDependant(dependant : ReactiveDependant[N]) {
    dependants += dependant
  }
  override def removeDependant(dependant : ReactiveDependant[N]) {
    dependants -= dependant
  }
  
  protected val lastNotification = new TransactionalTransientVariable[N]
  def publish(notification : N) {
    lastNotification.set(notification.transaction, notification)
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

  protected def notifyObservers(event: Transaction, value: A) {
    observers.foreach { _(value) }
  }
}