package reactive
package impl

import reactive.signals.Var
import java.util.UUID
import reactive.signals.Signal

import scala.language.higherKinds
class RoutableReactive[A, +OW[+_], +VW[+_], +PW[+_], R[+X] <: Reactive[X, OW, VW, PW, R]](initialValue: R[A]) extends ReactiveSource[R[A]] {
  this: R[A] =>
  type V = VW[A]
  type O = OW[A]
  type P = PW[A]

  protected val _input = Var(initialValue);
  override def <<(value: R[A]) = _input.<<(value)
  override protected[reactive] def emit(transaction: Transaction, value: R[A] /*, replyChannels: TicketAccumulator.Receiver**/ ) = _input.emit(transaction, value /*, replyChannels: _**/ )
  override protected[reactive] val uuid: UUID = _input.uuid

  val _output = _input.flatten[A, OW, VW, PW, R]
  protected[reactive] override def value(transaction: Transaction): V = _output.value(transaction)
  protected[reactive] override def pulse(transaction: Transaction): Option[P] = _output.pulse(transaction)
  protected[reactive] override def hasPulsed(transaction: Transaction): Boolean = _output.hasPulsed(transaction)
  protected[reactive] override def sourceDependencies(transaction: Transaction): Set[UUID] = _output.sourceDependencies(transaction)
  protected[reactive] override def isConnectedTo(transaction: Transaction): Boolean = _output.isConnectedTo(transaction);
  protected[reactive] override def addDependant(transaction: Transaction, dependant: Reactive.Dependant) = _output.addDependant(transaction, dependant)
  protected[reactive] override def removeDependant(transaction: Transaction, dependant: Reactive.Dependant) = _output.removeDependant(transaction, dependant)
  override def log: Signal[List[O]] = _output.log
  override def observe(obs: O => Unit) = _output.observe(obs)
  override def unobserve(obs: O => Unit) = _output.unobserve(obs)
  override def now: V = _output.now
}