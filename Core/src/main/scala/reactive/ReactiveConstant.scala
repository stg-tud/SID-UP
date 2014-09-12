package reactive

import java.util.UUID

trait ReactiveConstant[+O, +P] extends Reactive[O, P] {
  override def pulse(transaction: Transaction) = None
  override def hasPulsed(transaction: Transaction) = false
  override def sourceDependencies(transaction: Transaction) = Set[UUID]()
  override def isConnectedTo(transaction: Transaction) = false
  override def addDependant(transaction: Transaction, dependant: Reactive.Dependant): Boolean = false
  override def removeDependant(transaction: Transaction, dependant: Reactive.Dependant): Boolean = false
  override def observe(obs: O => Unit) = {}
  override def unobserve(obs: O => Unit) = {}
}
