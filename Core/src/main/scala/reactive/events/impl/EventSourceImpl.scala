package reactive
package events
package impl

import reactive.impl.ReactiveSourceImpl

class EventSourceImpl[A] extends EventStreamImpl[A] with ReactiveSourceImpl[A, A] with EventSource[A] {
  protected def makePulse(value: A): Option[A] = Some(value)
}
