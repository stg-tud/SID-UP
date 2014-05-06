package reactive.impl

import reactive.{Pulse, Transaction, Reactive}
import scala.concurrent.stm.{InTxn, TSet, atomic}
import com.typesafe.scalalogging.slf4j.StrictLogging
import scala.collection.LinearSeq

trait ReactiveImpl[O, P] extends Reactive[O, P] with DependencyImpl with ObservableImpl[O] with StrictLogging {
  private val currentTransactions: TSet[Transaction] = TSet[Transaction]()

  private[reactive] def addTransaction(transaction: Transaction): Unit = atomic { implicit tx =>
    transaction.addDependencies(currentTransactions)
    currentTransactions += transaction
  }

  override def isConnectedTo(transaction: Transaction) = !(transaction.sourceIDs & sourceDependencies(transaction)).isEmpty

  /**
   * get the pulse of this reactive
   * @param transaction each pulse is associated to a specific transaction
   * @return Some(pulse) if the reactive has a new pulse for the transaction, None if not
   */
  override def pulse(transaction: Transaction): Pulse[P] =  {
    if (isConnectedTo(transaction))
      transaction.pulse(this)
    else Pulse.noChange
  }

  override def hasPulsed(transaction: Transaction): Boolean = !isConnectedTo(transaction) || transaction.hasPulsed(this)

  protected def getObserverValue(transaction: Transaction, value: P): O

  /**
   * sets the pulse of the current transaction
   * this method is called with the calculated pulse after all dependencies have a fixed pulse for this transaction
   * @param transaction the transaction in which the pulse is valid
   * @param pulse the pulse of the transaction
   */
  protected[reactive] def setPulse(transaction: Transaction, pulse: Pulse[P]): Unit = {
    logger.trace(s"$this => Pulse($pulse) [${Option(transaction).map { _.uuid } }]")
    transaction.setPulse(this, pulse)
  }

  var commitHandlers = LinearSeq[Transaction => Unit]()

  def onCommit(code: Transaction => Unit): Unit = {
    commitHandlers :+= code
  }

  override protected[reactive] def commit(transaction: Transaction)(implicit tx: InTxn): Unit = {
    commitHandlers.foreach(_.apply(transaction))
    pulse(transaction).value.foreach(v => notifyObservers(transaction, getObserverValue(transaction, v)))
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
