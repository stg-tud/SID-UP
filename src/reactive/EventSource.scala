package reactive

import scala.collection.immutable.Map

class EventSource[A](name: String) extends StatelessEventStreamImpl[A](name) with ReactiveSource[A] {
  self => 
    
  def <<(value: A) = {
    super.emit(value);
  }

  protected[reactive] override def emit(event: Event, maybeValue: Option[A]) {
    propagate(event, maybeValue);
  }
}

object EventSource {
  def apply[A]() = new EventSource[A]("AnonEventSource");
  def apply[A](name: String) = new EventSource[A](name);
}