package reactive
package signals
package impl

import reactive.impl.ReactiveSourceImpl
import util.Util
import util.TicketAccumulator
import util.Update

class VarImpl[A](initialValue: A) extends SignalImpl[A](null, initialValue) with ReactiveSourceImpl[A] with Var[A] {
  override def emit(transaction: Transaction, newValue: A, replyChannels: TicketAccumulator.Receiver*) {
    publish(new Signal.Notification(transaction, noDependencyChange, value.update(newValue)), replyChannels: _*);
  }
}