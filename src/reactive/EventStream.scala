package reactive

import scala.collection.immutable.Map
import impl.FoldSignal
import reactive.impl.MergeStream
import reactive.impl.MappedEventStream
import reactive.impl.HoldSignal

trait EventStream[+A] extends Reactive[A] {
  def awaitMaybeEvent(event: Event): Option[A]
  def hold[B >: A](initialValue: B): Signal[B] = new HoldSignal(this, initialValue);
  def map[B](op: A => B): EventStream[B] = new MappedEventStream(this, op);
  def merge[B >: A](streams: EventStream[B]*): EventStream[B] = new MergeStream((this +: streams): _*);
  def fold[B](initialValue: B)(op: (B, A) => B): Signal[B] = new FoldSignal(initialValue, this, op);
}