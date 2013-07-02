package reactive
package events
package impl

import reactive.impl.ReactiveSourceImpl
import util.TicketAccumulator

class EventSourceImpl[A]() extends EventStreamImpl[A](null) with ReactiveSourceImpl[A] with EventSource[A] {
  override def emit(transaction : Transaction, value : A, replyChannels : TicketAccumulator.Receiver*) {
    publish(new EventStream.Notification(transaction, noDependencyChange, Some(value)), replyChannels :_*)
  }
}
