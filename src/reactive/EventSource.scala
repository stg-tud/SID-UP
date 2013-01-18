package reactive

import scala.collection.immutable.Map

class EventSource[A](name: String) extends EventStreamImpl[A](name) with ReactiveSource[A] {
  self => 
    
  def <<(value: A) = {
    super.emit(value);
  }

  protected[reactive] override def emit(event: Event) {
    notifyDependants(event);
  }

  protected[reactive] override def emit(event: Event, newValue: A) {
    notifyDependants(event, newValue);
  }
}

object EventSource {
  def apply[A]() = new EventSource[A]("AnonEventSource");
  def apply[A](name: String) = new EventSource[A](name);
}