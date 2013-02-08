package reactive.impl

import reactive.EventSource
import reactive.Event
import reactive.PropagationData

class EventSourceImpl[A](name: String) extends EventStreamImpl[A](name) with EventSource[A] {
  def <<(value: A) = {
    super.emit(value);
  }

  override def emit(event: Event, maybeValue: Option[A]) {
    notifyDependants(new PropagationData(event, Nil, Nil), maybeValue);
    maybeValue.foreach { notifyObservers(event, _) }
  }
}
