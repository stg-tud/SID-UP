package reactive
package events
package impl

import reactive.impl.ReactiveSourceImpl
import util.TicketAccumulator

class EventSourceImpl[A] extends EventStreamImpl[A] with ReactiveSourceImpl[A, A] with EventSource[A] {
  protected def makePulse(value: A): A = value
}
