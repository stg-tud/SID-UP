package reactive
package events

import reactive.signals.Signal

trait EventStream[+A] extends Reactive[A, A] {
  def hold[B >: A](initialValue: B): Signal[B]
  def map[B](op: A => B): EventStream[B]
  def collect[B](op: PartialFunction[A, B]): EventStream[B]
  def merge[B >: A](streams: EventStream[B]*): EventStream[B]
  def fold[B](initialValue: B)(op: (B, A) => B): Signal[B]
  def filter(op: A => Boolean): EventStream[A]
}
