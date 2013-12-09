package elmish
package signals
package impl

import elmish.impl.ReactiveImpl
import elmish.events.EventStream
import elmish.events.impl.FilteredEventStream
import elmish.events.impl.MappedEventStream
import elmish.events.impl.ChangesEventStream
import elmish.events.impl.PulseEventStream

trait SignalImpl[A] extends ReactiveImpl[A, A, A] with Signal[A] {

  override lazy val changes: EventStream[A] = new ChangesEventStream(this)
  override def map[B](op: A => B): Signal[B] = new MapSignal(this, op)
  override def flatMap[B](op: A => Signal[B]): Signal[B] = map(op).flatten
  override def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B] = new FlattenSignal(this.asInstanceOf[Signal[Signal[B]]]);
  override def log = new FoldSignal(List(now), changes, ((list: List[A], elem: A) => list :+ elem));
  override def snapshot(when: EventStream[_]): Signal[A] = pulse(when).hold(now);
  //TODO: has same name as  `Reactive.pulse(transaction: Transaction): Option[P]` but totally different semantics
  override def pulse(when: EventStream[_]): EventStream[A] = new PulseEventStream(this, when);

  protected override def getObserverValue(transaction: Transaction, pulseValue: A) = pulseValue
}
