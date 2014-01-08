package reactive

import java.util.UUID
import reactive.impl.mirroring.ReactiveMirror

import scala.language.higherKinds
trait ReactiveConstant[+X, +OW[+_], +VW[+_], +PW[+_], +R[Y] <: Reactive[Y, OW, VW, PW, R]] extends Reactive[X, OW, VW, PW, R] {
  this: R[X] =>
  override def pulse(transaction: Transaction) = None
  override def hasPulsed(transaction: Transaction) = false
  override def sourceDependencies(transaction: Transaction) = Set[UUID]()
  override def isConnectedTo(transaction: Transaction) = false
  override def addDependant(transaction: Transaction, dependant: Reactive.Dependant) {}
  override def removeDependant(transaction: Transaction, dependant: Reactive.Dependant) {}
  override def observe(obs: O => Unit) = {}
  override def unobserve(obs: O => Unit) = {}
}
