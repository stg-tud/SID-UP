package reactive
package signals
package impl

import reactive.events.EventStream
import reactive.events.impl.{ ChangesEventStream, DeltaEventStream, PulseEventStream }
import reactive.impl.ReactiveImpl
import scala.concurrent.stm._
import scala.language.higherKinds
import scala.collection.TraversableLike
import scala.collection.generic.CanBuildFrom
import reactive.events.TransposeEventStream

trait SignalImpl[A] extends ReactiveImpl[A, A] with Signal[A] {
  impl =>
  protected def value: Ref[A]
  protected def updateValue(tx: InTxn, newValue: A): Option[A] = {
    if (tx.synchronized(value.swap(newValue)(tx)) == newValue) {
      None
    } else {
      Some(newValue)
    }
  }

  override def now = atomic { tx => tx.synchronized(impl.value.get(tx)) }
  override lazy val changes: EventStream[A] = atomic { transactional.changes(_) }
  override lazy val delta = atomic { transactional.delta(_) }
  override def map[B](op: A => B): Signal[B] = atomic { transactional.map(op)(_) }
  override def tmap[B](op: (A, InTxn) => B): Signal[B] = atomic { transactional.tmap(op)(_) }
  override def flatMap[B](op: A => Signal[B]): Signal[B] = atomic { transactional.flatMap(op)(_) }
  override def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B] = atomic { transactional.flatten(evidence, _) }
  override def snapshot(when: EventStream[_]): Signal[A] = atomic { transactional.snapshot(when)(_) }
  override def pulse(when: EventStream[_]): EventStream[A] = atomic { transactional.pulse(when)(_) }
  override def transposeS[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[Signal[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]]): Signal[C[T]] = atomic { transactional.transposeS[T, C](evidence, canBuildFrom, _) }
  override def transposeE[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[EventStream[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]]): EventStream[C[T]] = atomic { transactional.transposeE[T, C](evidence, canBuildFrom, _) }

  protected override def getObserverValue(transaction: Transaction, pulseValue: A) = pulseValue
}

object SignalImpl {
  trait ViewImpl[A] extends ReactiveImpl.ViewImpl[A] with Signal.View[A] {
    override protected def impl: SignalImpl[A]
    override def now(implicit inTxn: InTxn) = inTxn.synchronized(impl.value())
    override def changes(implicit inTxn: InTxn): EventStream[A] = new ChangesEventStream(impl, inTxn)
    override def map[B](op: A => B)(implicit inTxn: InTxn): Signal[B] = new MapSignal(impl, op, inTxn)
    override def tmap[B](op: (A, InTxn) => B)(implicit inTxn: InTxn): Signal[B] = new TMapSignal(impl, op, inTxn)
    override def delta(implicit inTxn: InTxn): EventStream[(A, A)] = new DeltaEventStream(impl, inTxn)
    override def flatMap[B](op: A => Signal[B])(implicit inTxn: InTxn): Signal[B] = map(op).flatten
    override def tflatMap[B](op: (A, InTxn) => Signal[B])(implicit inTxn: InTxn): Signal[B] = tmap(op).flatten
    override def flatten[B](implicit evidence: A <:< Signal[B], inTxn: InTxn): Signal[B] = new FlattenSignal(impl.asInstanceOf[Signal[Signal[B]]], inTxn)
    override def snapshot(when: EventStream[_])(implicit inTxn: InTxn): Signal[A] = pulse(when).hold(now)
    override def pulse(when: EventStream[_])(implicit inTxn: InTxn): EventStream[A] = new PulseEventStream(impl, when, inTxn)
    override def log(implicit inTxn: InTxn) = new FoldSignal(List(now), changes, (list: List[A], elem: A) => list :+ elem, inTxn)
    override def transposeS[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[Signal[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]], inTxn: InTxn): Signal[C[T]] = new TransposeSignal(/*map(evidence)*/impl.asInstanceOf[Signal[C[Signal[T]]]], inTxn)
    override def transposeE[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[EventStream[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]], inTxn: InTxn): EventStream[C[T]] = new TransposeEventStream(/*map(evidence)*/impl.asInstanceOf[Signal[C[EventStream[T]]]], inTxn)
  }
}
