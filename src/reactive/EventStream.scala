package reactive

import scala.collection.immutable.Map

trait EventStream[+A] extends Reactive[A] {
  def awaitMaybeEvent(event: Event): Option[A]
  def hold[B >: A](initialValue: B): Signal[B]
  def map[B](op: A => B): EventStream[B]
  def merge[B >: A](streams: EventStream[_ <: B]*): EventStream[B]
  def fold[B](initialValue: B)(op : (B, A) => B) : Signal[B]
}