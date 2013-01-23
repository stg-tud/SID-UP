package reactive

import scala.collection.immutable.Map
import reactive.impl.EventSourceImpl
import reactive.impl.EventSourceImpl

trait EventSource[A] extends EventStream[A] with ReactiveSource[A] {
  def <<(value: A) : Event
}

object EventSource {
  def apply[A]() : EventSource[A] = apply[A]("AnonEventSource");
  def apply[A](name: String) : EventSource[A] = new EventSourceImpl[A](name);
}