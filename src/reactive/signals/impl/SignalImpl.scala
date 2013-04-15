package reactive
package signals
package impl

import java.util.UUID
import reactive.impl.ReactiveImpl
import reactive.events.EventStream
import reactive.events.EventNotification
import util.Util
import reactive.events.impl.FilteredEventStream
import reactive.events.impl.MappedEventStream
import reactive.events.impl.ChangesEventStream
import util.MutableValue
import util.MutableValue

abstract class SignalImpl[A](sourceDependencies: Set[UUID], initialValue: A) extends ReactiveImpl[A, SignalNotification[A]](sourceDependencies) with Signal[A] {
  signal =>
  protected val value = new MutableValue(initialValue)
  override def now = value.current
  override def apply()(implicit t : Transaction) = value.current
  override val changes: EventStream[A] = new ChangesEventStream(this)
  override def map[B](op: A => B): Signal[B] = new MapSignal(this, op)
//  override def rmap[B](op: A => Signal[B]): Signal[B] = map(op)(t).flatten
//  override def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B] = new FlattenSignal(this.asInstanceOf[Signal[Signal[B]]]);
  override def log = new FoldSignal(List(now), changes, ((list: List[A], elem: A) => list :+ elem));
  override def snapshot(when: EventStream[_]): Signal[A] = new SnapshotSignal(this, when);
}
