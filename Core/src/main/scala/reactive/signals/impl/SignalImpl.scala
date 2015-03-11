package reactive
package signals
package impl

import reactive.impl.ReactiveImpl
import reactive.events.EventStream
import reactive.events.impl.ChangesEventStream
import reactive.events.impl.DeltaEventStream
import reactive.events.impl.PulseEventStream
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import reactive.events.TransposeEventStream
import scala.collection.TraversableLike
import reactive.lifting.Lift

trait SignalImpl[A] extends ReactiveImpl[A, A] with Signal[A] {

  override lazy val changes: EventStream[A] = new ChangesEventStream(this)
  override lazy val delta = new DeltaEventStream(this)
  override def map[B](op: A => B): Signal[B] = new MapSignal(this, op)
  override def flatMap[B](op: A => Signal[B]): Signal[B] = map(op).flatten
  override def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B] = new FlattenSignal( /*this.map(evidence)*/ this.asInstanceOf[Signal[Signal[B]]])
  override def log = new FoldSignal(List(now), changes, ((list: List[A], elem: A) => list :+ elem))
  override def snapshot(when: EventStream[_]): Signal[A] = pulse(when).hold(now)
  //TODO: has same name as  `Reactive.pulse(transaction: Transaction): Option[P]` but totally different semantics
  override def pulse(when: EventStream[_]): EventStream[A] = new PulseEventStream(this, when)
  override def transposeS[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[Signal[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]]) = new TransposeSignal[T, C]( /*this.map(evidence)*/ this.asInstanceOf[Signal[C[Signal[T]]]])
  override def transposeE[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[EventStream[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]]): EventStream[C[T]] = new TransposeEventStream[T, C]( /*this.map(evidence)*/ this.asInstanceOf[Signal[C[EventStream[T]]]])
  override def ===(other: Signal[_]): Signal[Boolean] = Lift.signal2((_: Any) == (_: Any))(this, other)
  protected override def getObserverValue(transaction: Transaction, pulseValue: A) = pulseValue
}
