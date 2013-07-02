package reactive

import java.util.UUID

trait ReactiveConstant[+O, +V, +P] extends Reactive[O, V, P] {
  override val sourceDependencies = Set[UUID]()
  override def isConnectedTo(transaction : Transaction) = false
  override def addDependant(maybeTransaction : Option[Transaction], dependant : ReactiveDependant[P]) = None
  override def removeDependant(dependant : ReactiveDependant[P]) = {}
  override def observe(obs: O => Unit) = {}
  override def unobserve(obs: O => Unit) = {}
}
