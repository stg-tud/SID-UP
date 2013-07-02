package reactive
package impl

import java.util.UUID
import scala.collection.mutable
import util.MutableValue
import util.TransactionalTransientVariable
import util.TicketAccumulator

abstract class ReactiveImpl[O, V, P](initialSourceDependencies: Set[UUID]) extends Reactive[O, V, P] {
  private val accu = new TicketAccumulator
  protected val _sourceDependencies = new MutableValue(initialSourceDependencies);
  override def sourceDependencies = _sourceDependencies.current

  override def isConnectedTo(transaction: Transaction) = !(transaction.sources & sourceDependencies).isEmpty
  private var dependants = Set[ReactiveDependant[P]]()

  override def addDependant(maybeTransaction: Option[Transaction], dependant: ReactiveDependant[P]) = {
    dependants += dependant
    maybeTransaction.flatMap { _lastNotification.getIfSet(_) }
  }

  override def removeDependant(dependant: ReactiveDependant[P]) {
    dependants -= dependant
  }

  protected val _lastNotification = new TransactionalTransientVariable[ReactiveNotification[P]]
  def publish(notification: ReactiveNotification[P], replyChannels : TicketAccumulator.Receiver*) {
    if(replyChannels.isEmpty) throw new IllegalArgumentException("Requires at least one reply channel!");
    _lastNotification.set(notification.transaction, notification)
    accu.initializeForNotification(dependants.size)(replyChannels :_*)
    dependants.foreach(_.notify(accu, notification));
  }
  // ====== Observing stuff ======

  private val observers = mutable.Set[O => Unit]()
  def observe(obs: O => Unit) {
    observers += obs
  }
  def unobserve(obs: O => Unit) {
    observers -= obs
  }

  protected def notifyObservers(value: O) {
    observers.foreach { _(value) }
  }
}