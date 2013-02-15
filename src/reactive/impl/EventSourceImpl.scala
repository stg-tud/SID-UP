package reactive.impl

import reactive.EventSource
import reactive.Event

class EventSourceImpl[A](name: String) extends StatelessEventStreamImpl[A](name) with EventSource[A] {
  def <<(value: A) = {
    super.emit(value);
  }

  override def emit(event: Event, maybeValue: Option[A]) {
    propagate(event, maybeValue);
  }
}
