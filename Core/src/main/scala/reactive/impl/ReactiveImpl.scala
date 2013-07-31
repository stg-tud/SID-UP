package reactive
package impl

import scala.collection.mutable
import com.typesafe.scalalogging.slf4j.Logging

trait ReactiveImpl[O, V, P] extends Reactive[O, V, P] with Logging {
  override def isConnectedTo(transaction: Transaction) = !(transaction.sources & sourceDependencies(transaction)).isEmpty

  private[reactive] val name = {
    val classname = getClass.getName
    val unqualifiedClassname = classname.substring(classname.lastIndexOf('.') + 1)
    s"${unqualifiedClassname}(${hashCode})"
  }
  override def toString = name

  private var currentTransaction: Transaction = _
  private var pulse: Option[P] = None
  def pulse(transaction: Transaction): Option[P] = if(currentTransaction == transaction) pulse else None
  def hasPulsed(transaction: Transaction): Boolean = currentTransaction == transaction

  private var dependants = Set[Reactive.Dependant]()
  override def addDependant(transaction: Transaction, dependant: Reactive.Dependant) {
    logger.trace(s"$dependant <~ $this [${Option(transaction).map{_.uuid}}]")
    dependants += dependant
  }
  override def removeDependant(transaction: Transaction, dependant: Reactive.Dependant) {
    logger.trace(s"$dependant <!~ $this [${Option(transaction).map{_.uuid}}]")
    dependants -= dependant
  }

  protected[reactive] def doPulse(transaction: Transaction, sourceDependenciesChanged: Boolean, pulse: Option[P]) {
    logger.trace(s"$this => Pulse($pulse, $sourceDependenciesChanged) [${Option(transaction).map{_.uuid}}]")
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
    logger.trace(s"$this observers: ${observers.size}")
  }
  def unobserve(obs: O => Unit) {
    observers -= obs
    logger.trace(s"$this observers: ${observers.size}")
  }

  private def notifyObservers(transaction: Transaction, value: O) {
    logger.trace(s"$this -> Observers(${observers.size})")
    observers.foreach { _(value) }
  }
}
