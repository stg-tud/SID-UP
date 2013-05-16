package reactive
package events
package impl

import reactive.impl.ReactiveImpl
import reactive.signals.Signal
import java.util.UUID
import reactive.signals.impl.FoldSignal
import reactive.signals.impl.HoldSignal
import util.TransactionalAccumulator
import util.TransactionalTransientVariable

abstract class EventStreamImpl[A](sourceDependencies: Set[UUID]) extends ReactiveImpl[A, EventNotification[A]](sourceDependencies) with EventStream[A] {
  def apply()(implicit t: Transaction): Option[A] = _lastNotification.get(t).maybeValue
  override def hold[B >: A](initialValue: B): Signal[B] = new HoldSignal(this, initialValue);
  override def map[B](op: A => B): EventStream[B] = new MappedEventStream(this, op);
  override def merge[B >: A](streams: EventStream[B]*): EventStream[B] = new MergeStream(this :: streams.toList);
  override def fold[B](initialValue: B)(op: (B, A) => B): Signal[B] = new FoldSignal(initialValue, this, op);
  override def log = fold(List[A]())((list, elem) => list :+ elem)
  override def filter(op: A => Boolean): EventStream[A] = new FilteredEventStream(this, op);

  override def publish(notification: EventNotification[A]) {
    super.publish(notification)
    notification.maybeValue.foreach { notifyObservers(_) };
  }
}