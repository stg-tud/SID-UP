package reactive

import java.util.UUID
import scala.concurrent.stm.InTxn

trait ReactiveConstant[+O, +P] extends Reactive[O, P] {
  override def pulse(transaction: Transaction) = Pulse()
  override def hasPulsed(transaction: Transaction) = false
  override def sourceDependencies(transaction: Transaction) = Set[UUID]()
  override def isConnectedTo(transaction: Transaction) = false
  override def addDependant(transaction: Transaction, dependant: Reactive.Sink): Unit = ()
  //override def removeDependant(transaction: Transaction, dependant: Reactive.Dependant) {}
  override def observe(obs: O => Unit): Unit = {}
  override def unobserve(obs: O => Unit): Unit = {}
  override protected[reactive] def commit(transaction: Transaction)(implicit tx: InTxn): Unit =
    throw new UnsupportedOperationException("reactive constants can not be commited")
}
