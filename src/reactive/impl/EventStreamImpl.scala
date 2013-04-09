package reactive
package impl

import Reactive._

abstract class EventStreamImpl[A](name: String) extends ReactiveImpl[A](name) with EventStream[A] {
  override def hold[B >: A](initialValue: B)(t: Txn): Signal[B] = new HoldSignal(this, initialValue, t);
  override def map[B](op: A => B)(t: Txn): EventStream[B] = new MappedEventStream(this, op, t);
  override def merge[B >: A](streams: EventStream[B]*)(t: Txn): EventStream[B] = new MergeStream(this :: streams.toList, t);
  override def fold[B](initialValue: B)(op: (B, A) => B)(t: Txn): Signal[B] = new FoldSignal(initialValue, this, op, t);
  override def log(t: Txn) = fold(List[A]())((list, elem) => list :+ elem)(t)
  override def filter(op: A => Boolean)(t: Txn): EventStream[A] = new FilteredEventStream(this, op, t);
}