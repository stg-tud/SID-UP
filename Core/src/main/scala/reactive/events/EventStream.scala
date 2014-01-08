package reactive
package events

import reactive.signals.Signal

trait EventStream[+A] extends Reactive[A, Reactive.IDENTITY, Reactive.UNIT, Reactive.IDENTITY, EventStream] {
  def hold[B >: A](initialValue: B): Signal[B]
  def map[B](op: A => B): EventStream[B]
  def merge[B >: A](streams: EventStream[B]*): EventStream[B]
  def fold[B](initialValue: B)(op: (B, A) => B): Signal[B]
  def filter(op: A => Boolean): EventStream[A]
}
