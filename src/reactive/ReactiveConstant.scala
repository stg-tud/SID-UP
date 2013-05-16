package reactive

import java.util.UUID

trait ReactiveConstant[+A, N <: ReactiveNotification[A]] extends Reactive[A, N] {
  override val sourceDependencies = Set[UUID]()
  override def isConnectedTo(transaction : Transaction) = false
  override def addDependant(maybeTransaction : Option[Transaction], dependant : ReactiveDependant[N]) = None
  override def maybeNotification(transaction : Transaction) = None
  override def removeDependant(dependant : ReactiveDependant[N]) = {}
  override def observe(obs: A => Unit) = {}
  override def unobserve(obs: A => Unit) = {}
}