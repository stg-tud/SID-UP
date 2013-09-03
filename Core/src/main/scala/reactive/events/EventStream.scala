package reactive
package events

import reactive.signals.Signal
import reactive.impl.mirroring.EventStreamMirror

trait EventStream[+A] extends Reactive[A, Unit, A, EventStream[A]] {
  def hold[B >: A](initialValue: B): Signal[B]
  def map[B](op: A => B): EventStream[B]
  def merge[B >: A](streams: EventStream[B]*): EventStream[B]
  def fold[B](initialValue: B)(op: (B, A) => B): Signal[B]
  def filter(op: A => Boolean): EventStream[A]
}
