package reactive

import java.util.UUID

import scala.concurrent.stm.InTxn

trait ReactiveConstant[+O, +P] extends Reactive[O, P] {
  override def pulse(tx: InTxn) = Reactive.Unchanged
  override def hasPulsed(tx: InTxn) = false
  override val singleSourceDependencies = Set[UUID]()
  override def sourceDependencies(tx: InTxn) = Set[UUID]()
  override def isConnectedTo(transaction: Transaction) = false
  override def addDependant(tx: InTxn, dependant: Reactive.Dependant) = {}
  override def removeDependant(tx: InTxn, dependant: Reactive.Dependant) = {}
  override def observe(obs: O => Unit) = {}
  override def unobserve(obs: O => Unit) = {}
}

object ReactiveConstant {
  trait View[+O] extends Reactive.View[O] {
    override def observe(obs: O => Unit)(implicit tx: InTxn) = {}
    override def unobserve(obs: O => Unit)(implicit tx: InTxn) = {}
  }
}
