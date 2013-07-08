package reactive

import java.util.UUID

trait ReactiveConstant[+O, +V, +P] extends Reactive[O, V, P] {
  override def pulse(t: Transaction) = None
  override def hasPulsed(t: Transaction) = false
  override def sourceDependencies = Set[UUID]()
  override def isConnectedTo(transaction: Transaction) = false
  override def addDependant(dependant: Reactive.Dependant) {}
  override def removeDependant(dependant: Reactive.Dependant) {}
  override def observe(obs: O => Unit) = {}
  override def unobserve(obs: O => Unit) = {}
}
