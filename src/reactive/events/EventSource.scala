package reactive
package events

trait EventSource[A] extends EventStream[A] with ReactiveSource[A, EventNotification[A]]

object EventSource {
  def apply[A]() : EventSource[A] = new impl.EventSourceImpl[A];
}