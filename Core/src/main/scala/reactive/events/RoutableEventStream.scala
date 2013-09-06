package reactive
package events

import reactive.signals.Var
import java.util.UUID
import reactive.ReactiveSource
import reactive.Transaction
import reactive.impl.RoutableReactive
import reactive.signals.Signal

trait RoutableEventStream[A] extends EventStream[A] with ReactiveSource[EventStream[A]]

object RoutableEventStream {
  def apply[A](initialValue: EventStream[A]): RoutableEventStream[A] = new RoutableReactive[A, Unit, A, EventStream[A]](initialValue) with RoutableEventStream[A] {
    def hold[B >: A](initialValue: B): Signal[B] = _output.hold(initialValue)
    def map[B](op: A => B): EventStream[B] = _output.map(op)
    def merge[B >: A](streams: EventStream[B]*): EventStream[B] = _output.merge(streams: _*)
    def fold[B](initialValue: B)(op: (B, A) => B): Signal[B] = _output.fold(initialValue)(op)
    def filter(op: A => Boolean): EventStream[A] = _output.filter(op)
  }
}