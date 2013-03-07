package reactive.impl

import reactive.EventSource
import reactive.Transaction

class EventSourceImpl[A](name: String) extends EventStreamImpl[A](name) with ReactiveSourceImpl[A] with EventSource[A] {
  def <<(value: A) {
    super.emit(value);
  }
}
