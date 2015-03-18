package reactive
package events
package impl

import reactive.impl.ReactiveImpl
import reactive.signals.Signal
import reactive.signals.impl.FoldSignal

trait EventStreamImpl[A] extends ReactiveImpl[A, A] with EventStream[A] {
  override def hold[B >: A](initialValue: B): Signal[B] = fold(initialValue) { (_, value) => value }
  override def map[B](op: A => B): EventStream[B] = new TransformEventStream[B, A](this, _.map(op));
  override def collect[B](op: PartialFunction[A, B]): EventStream[B] = new TransformEventStream[B, A](this, _.collect(op));
  override def deOption[B](implicit evidence: A <:< Option[B]): EventStream[B] = new TransformEventStream[B, A](this, _.flatten)
  override def mapOption[B](op: A => Option[B]): EventStream[B] = new TransformEventStream[B, A](this, _.flatMap(op));
  override def filter(op: A => Boolean): EventStream[A] = new TransformEventStream[A, A](this, _.filter(op));
  override def merge[B >: A](streams: EventStream[B]*): EventStream[B] = new MergeStream(this :: streams.toList);
  override def fold[B](initialValue: B)(op: (B, A) => B): Signal[B] = new FoldSignal(initialValue, this, op);
  override def log = fold(List[A]())((list, elem) => list :+ elem)

  protected override def getObserverValue(transaction: Transaction, pulseValue: A) = pulseValue
}
