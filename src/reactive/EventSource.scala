package reactive


trait EventSource[A] extends EventStream[A] with ReactiveSource[A] {
  def <<(value: A)
}

object EventSource {
  def apply[A]() : EventSource[A] = apply[A]("AnonEventSource");
  def apply[A](name: String) : EventSource[A] = new impl.EventSourceImpl[A](name);
}