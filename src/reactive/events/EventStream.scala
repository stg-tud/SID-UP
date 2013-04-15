package reactive
package events

import reactive.signals.Signal

trait EventStream[+A] extends Reactive[A, EventNotification[A]] {
  def apply()(implicit t: Transaction): Option[A]
  def hold[B >: A](initialValue: B): Signal[B]
  def map[B](op: A => B): EventStream[B]
  //  def merge[B >: A](streams: EventStream[B]*): EventStream[B]
  def fold[B](initialValue: B)(op: (B, A) => B): Signal[B]
  def filter(op: A => Boolean): EventStream[A]
}

object EventStream {
  type Dependant[-A] = ReactiveDependant[EventNotification[A]]
}