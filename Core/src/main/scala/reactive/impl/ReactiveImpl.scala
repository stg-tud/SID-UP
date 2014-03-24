package reactive.impl

import reactive.{Transaction, Reactive}
import scala.concurrent.stm.TSet
import scala.concurrent.stm.atomic
import com.typesafe.scalalogging.slf4j.Logging

trait ReactiveImpl[O, P] extends Reactive[O, P] with DependencyImpl with ObservableImpl[O] with Logging {
  private val currentTransactions: TSet[Transaction] = TSet[Transaction]()

  private[reactive] def addTransaction(transaction: Transaction): Unit = atomic { implicit tx =>
    transaction.addDependencies(currentTransactions)
    currentTransactions += transaction
  }

  override def isConnectedTo(transaction: Transaction) = !(transaction.sources & sourceDependencies(transaction)).isEmpty

  /**
   * get the pulse of this reactive
   * @param transaction each pulse is associated to a specific transaction
   * @return Some(pulse) if the reactive has a new pulse for the transaction, None if not
   */
  def pulse(transaction: Transaction): Option[P] = atomic { implicit tx =>
    if (currentTransactions(transaction))
      transaction.pulse(this)
    else None
  }

  def hasPulsed(transaction: Transaction): Boolean = ???

  protected def getObserverValue(transaction: Transaction, value: P): O

  /**
   * sets the pulse of the current transaction
   * this method is called with the calculated pulse after all dependencies have a fixed pulse for this transaction
   * @param transaction the transaction in which the pulse is valid
   * @param pulse the pulse of the transaction
   */
  protected[reactive] def setPulse(transaction: Transaction, pulse: Option[P]): Unit = {
    logger.trace(s"$this => Pulse($pulse) [${Option(transaction).map { _.uuid } }]")
    transaction.setPulse(this, pulse)
  }

  /**
   * name is used for logging purposes
   */
  protected[reactive] val name = {
    val classname = getClass.getName
    val unqualifiedClassname = classname.substring(classname.lastIndexOf('.') + 1)
    s"$unqualifiedClassname($hashCode)"
  }

  override def toString = name

}
