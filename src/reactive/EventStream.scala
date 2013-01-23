package reactive

import scala.collection.immutable.Map

trait EventStream[A] extends Reactive[A] {
  def awaitMaybeEvent(event: Event): Option[A]
  def hold(initialValue: A): Signal[A] = new HoldSignal(this, initialValue);
  def map[B](op: A => B): EventStream[B] = new MappedEventStream(this, op);
  def merge[B >: A](streams: EventStream[_ <: B]*): EventStream[B] = new MergeStream((this +: streams): _*);
  def fold[B](initialValue: B)(op : (B, A) => B) : Signal[B] = new FoldSignal(initialValue, this, op);
}