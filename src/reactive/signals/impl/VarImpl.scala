package reactive
package signals
package impl

import reactive.impl.ReactiveSourceImpl
import util.Util
import util.TicketAccumulator

class VarImpl[A](initialValue: A) extends SignalImpl[A](null, initialValue) with ReactiveSourceImpl[A] with Var[A] {
  override def emit(transaction: Transaction, newValue: A, replyChannels: TicketAccumulator.Receiver*) {
    publish(new SignalNotification(transaction, noDependencyChange, value.update(newValue)), replyChannels: _*);
  }
}