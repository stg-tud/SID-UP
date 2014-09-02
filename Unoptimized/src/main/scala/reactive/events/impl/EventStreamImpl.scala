package reactive
package events
package impl

import reactive.impl.ReactiveImpl
import reactive.signals.Signal
import reactive.signals.impl.FoldSignal

trait EventStreamImpl[A] extends ReactiveImpl[A, A] with EventStream[A] {
  override def hold[B >: A](initialValue: B): Signal[B] = fold(initialValue) { (_, value) => value }
  override def map[B](op: A => B): EventStream[B] = new MappedEventStream(this, op);
  override def collect[B](op: PartialFunction[A, B]): EventStream[B] = new PartiallyMappedEventStream(this, op);
  override def merge[B >: A](streams: EventStream[B]*): EventStream[B] = new MergeStream(this :: streams.toList);
  override def fold[B](initialValue: B)(op: (B, A) => B): Signal[B] = new FoldSignal(initialValue, this, op);
  override def log = fold(List[A]())((list, elem) => list :+ elem)
  override def filter(op: A => Boolean): EventStream[A] = new FilteredEventStream(this, op);

  protected override def getObserverValue(transaction: Transaction, pulseValue: A) = pulseValue
}