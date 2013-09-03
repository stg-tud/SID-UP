package reactive.impl

import reactive.ReactiveSource
import reactive.Reactive
import reactive.signals.Var
import java.util.UUID
import reactive.Transaction

class RoutableReactive[R <: Reactive[_,_,_,R]](initialValue: R) extends ReactiveSource[R] {
	protected val _input = Var(initialValue);
    override def <<(value: R) = _input.<<(value)
    override protected[reactive] def emit(transaction: Transaction, value: R /*, replyChannels: TicketAccumulator.Receiver**/ ) = _input.emit(transaction, value /*, replyChannels: _**/ )
    override protected[reactive] val uuid: UUID = _input.uuid

    val _output = _input.flatten

}