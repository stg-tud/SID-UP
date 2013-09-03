package reactive
package events

import reactive.signals.Var
import java.util.UUID
import reactive.ReactiveSource
import reactive.Transaction
import reactive.impl.RoutableReactive

trait RoutableEventStream[A] extends EventStream[A] with ReactiveSource[EventStream[A]]

object RoutableEventStream {
  def apply[A](initialValue: EventStream[A]): RoutableEventStream[A] = new RoutableReactive[EventStream[A]](initialValue) with RoutableEventStream[A] {
    
  }
}