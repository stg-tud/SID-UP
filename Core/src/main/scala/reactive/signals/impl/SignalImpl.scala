package reactive
package signals
package impl

import reactive.impl.ReactiveImpl
import reactive.events.EventStream
import reactive.events.impl.FilteredEventStream
import reactive.events.impl.MappedEventStream
import reactive.events.impl.ChangesEventStream
import reactive.events.impl.PulseEventStream
import reactive.impl.mirroring.SignalMirror

trait SignalImpl[A] extends ReactiveImpl[A, A, A, Signal[A]] with Signal[A] {

  override lazy val changes: EventStream[A] = new ChangesEventStream(this)
  override def map[B](op: A => B): Signal[B] = new MapSignal(this, op)
  override def flatMap[B](op: A => Signal[B]): Signal[B] = map(op).flatten
  def flatten[R <: Reactive[_, _, _, R]](implicit evidence: A <:< R): R = {
    // this would be the way to do it without a type cast, but that only costs
    // pointless performance since <:< is just the identitiy function...
    // FlattenSignal(this.map(evidence));
    FlattenSignal(this.asInstanceOf[Signal[R]]);
  }
  override def log = new FoldSignal(List(now), changes, ((list: List[A], elem: A) => list :+ elem));
  override def snapshot(when: EventStream[_]): Signal[A] = pulse(when).hold(now);
  //TODO: has same name as  `Reactive.pulse(transaction: Transaction): Option[P]` but totally different semantics
  override def pulse(when: EventStream[_]): EventStream[A] = new PulseEventStream(this, when);

  protected override def getObserverValue(transaction: Transaction, pulseValue: A) = pulseValue
}
