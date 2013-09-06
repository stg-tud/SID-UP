package reactive
package impl

import reactive.signals.Var
import java.util.UUID
import reactive.signals.Signal

class RoutableReactive[O, V, P, R <: Reactive[O, V, P, R]](initialValue: R) extends ReactiveSource[R] {
  this: R =>
  protected val _input = Var(initialValue);
  override def <<(value: R) = _input.<<(value)
  override protected[reactive] def emit(transaction: Transaction, value: R /*, replyChannels: TicketAccumulator.Receiver**/ ) = _input.emit(transaction, value /*, replyChannels: _**/ )
  override protected[reactive] val uuid: UUID = _input.uuid

  val _output = _input.flatten
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