package reactive
package events
package impl

import reactive.impl.ReactiveSourceImpl

class EventSourceImpl[A] extends EventStreamImpl[A] with ReactiveSourceImpl[A, A] with EventSource[A] {
  override protected def makePulse(value: A): Pulse[A] = Pulse.change(value)
}
