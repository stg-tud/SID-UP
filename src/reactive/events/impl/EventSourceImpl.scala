package reactive
package events
package impl

import reactive.impl.ReactiveSourceImpl

class EventSourceImpl[A]() extends EventStreamImpl[A](null) with ReactiveSourceImpl[A] with EventSource[A] {
  override def emit(transaction : Transaction, value : A) {
    publish(new EventNotification(transaction, noDependencyChange, Some(value)))
  }
}
