package reactive
package signals

import reactive.events.{ EventStream, NothingEventStream }
import scala.concurrent.stm._
import scala.collection.TraversableLike
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import reactive.signals.impl.FunctionalSignal
import reactive.events.impl.DependentEventStreamImpl
import reactive.impl.MultiDependentReactive

case class Val[A](value: A) extends Signal[A] with ReactiveConstant[A, A] {
  impl =>
  override def withName(name: String) = this

  override val now = value
  override val delta: EventStream[(A, A)] = NothingEventStream
  override lazy val log: Signal[List[A]] = new Val(List(value))
  override val changes: EventStream[A] = NothingEventStream
  override def map[B](op: A => B): Signal[B] = new Val(op(value))
  override def tmap[B](op: (A, InTxn) => B): Signal[B] = new Val(atomic { op(value, _) })
  override def flatMap[B](op: A => Signal[B]): Signal[B] = op(value)
  override def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B] = evidence(value)
  override def snapshot(when: EventStream[_]): Signal[A] = impl
  override def pulse(when: EventStream[_]): EventStream[A] = when.map { _ => value }
  override def transposeS[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[Signal[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]]): Signal[C[T]] = atomic { transactional.transposeS[T, C](evidence, canBuildFrom, _) }
  override def transposeE[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[EventStream[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]]): EventStream[C[T]] = atomic { transactional.transposeE[T, C](evidence, canBuildFrom, _) }

  override object transactional extends Signal.View[A] with ReactiveConstant.View[A] {
    override def now(implicit inTxn: InTxn) = impl.now
    override def delta(implicit inTxn: InTxn): EventStream[(A, A)] = impl.delta
    override def log(implicit inTxn: InTxn): Signal[List[A]] = impl.log
    override def changes(implicit inTxn: InTxn): EventStream[A] = impl.changes
    override def map[B](op: A => B)(implicit inTxn: InTxn): Signal[B] = impl.map(op)
    override def tmap[B](op: (A, InTxn) => B)(implicit inTxn: InTxn): Signal[B] = new Val(op(value, inTxn))
    override def flatMap[B](op: A => Signal[B])(implicit inTxn: InTxn): Signal[B] = impl.flatMap(op)
    override def tflatMap[B](op: (A, InTxn) => Signal[B])(implicit inTxn: InTxn): Signal[B] = op(value, inTxn)
    override def flatten[B](implicit evidence: A <:< Signal[B], inTxn: InTxn): Signal[B] = impl.flatten
    override def snapshot(when: EventStream[_])(implicit inTxn: InTxn): Signal[A] = impl.snapshot(when)
    override def pulse(when: EventStream[_])(implicit inTxn: InTxn): EventStream[A] = when.transactional.map { _ => value }(inTxn)
    override def transposeS[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[Signal[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]], inTxn: InTxn): Signal[C[T]] = new FunctionalSignal({ tx: InTxn =>
      value.map(_.transactional.now(tx))
    }, value.toSet, inTxn)
    override def transposeE[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[EventStream[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]], inTxn: InTxn): EventStream[C[T]] = new MultiDependentReactive(inTxn) with DependentEventStreamImpl[C[T]] {
      override val dependencies = value.toSet : Set[Reactive.Dependency]
	  override protected def reevaluate(tx: InTxn): Option[C[T]] = {
	    // get the flat list of pulses, if none changed, we also propagate no change
	    val pulses = value.flatMap(_.pulse(tx).asOption)
	    if (pulses.isEmpty) None else Some(pulses)
	  }
    }
  }
}
