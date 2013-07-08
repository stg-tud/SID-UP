package reactive
package impl

import java.util.UUID
import scala.collection.mutable
import util.MutableValue
import util.TransactionalTransientVariable
import util.TicketAccumulator

trait ReactiveImpl[O, V, P] extends Reactive[O, V, P] {
  override def isConnectedTo(transaction: Transaction) = !(transaction.sources & sourceDependencies(transaction)).isEmpty

  private var currentTransaction: Transaction = _
  private var pulse: Option[P] = _
  def pulse(transaction: Transaction): Option[P] = pulse
  def hasPulsed(transaction: Transaction): Boolean = currentTransaction == transaction

  private var dependants = Set[Reactive.Dependant]()
  override def addDependant(transaction: Transaction, dependant: Reactive.Dependant) {
    dependants += dependant
  }
  override def removeDependant(transaction: Transaction, dependant: Reactive.Dependant) {
    dependants -= dependant
  }

  protected[reactive] def doPulse(transaction: Transaction, sourceDependenciesChanged: Boolean, pulse: Option[P]) {
    this.pulse = pulse
    this.currentTransaction = transaction;
    val pulsed = pulse.isDefined
    dependants.foreach(_.apply(transaction, sourceDependenciesChanged, pulsed));
    if (pulsed) {
      val value = getObserverValue(transaction, pulse.get);
      notifyObservers(transaction, value)
    }
  }
  protected def getObserverValue(transaction: Transaction, pulseValue: P): O

  // ====== Observing stuff ======

  private val observers = mutable.Set[O => Unit]()
  def observe(obs: O => Unit) {
    observers += obs
  }
  def unobserve(obs: O => Unit) {
    observers -= obs
  }
  
  private def notifyObservers(transaction: Transaction, value: O) {
    observers.foreach { _(value) }
  }
}