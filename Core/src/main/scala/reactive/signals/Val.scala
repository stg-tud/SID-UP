package reactive
package signals

import reactive.events.EventStream
import reactive.events.NothingEventStream
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import reactive.events.TransposeEventStream
import scala.collection.TraversableLike
import reactive.signals.impl.FunctionalSignal
import reactive.events.impl.DependentEventStreamImpl
import reactive.impl.MultiDependentReactive

@SerialVersionUID(1234837843483487L)
class Val[A](val value: A) extends Signal[A] with ReactiveConstant[A, A] with Serializable {
  override val now = value
  override val delta = NothingEventStream
  override def value(t: Transaction) = value
  override lazy val log = new Val(List(value))
  override val changes: EventStream[A] = NothingEventStream
  override def map[B](op: A => B): Signal[B] = new Val(op(value))
  override def flatMap[B](op: A => Signal[B]): Signal[B] = op(value)
  override def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B] = value
  override def snapshot(when: EventStream[_]): Signal[A] = this
  override def pulse(when: EventStream[_]): EventStream[A] = when.map { _ => value }
  override def transposeS[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[Signal[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]]) = new FunctionalSignal({ transaction => evidence(value).map(_.value(transaction)) }, value.toSet)
  override def transposeE[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[EventStream[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]]) = new DependentEventStreamImpl[C[T]] with MultiDependentReactive {
    override val dependencies = value.toSet[Reactive[_, _]]
    protected def reevaluate(transaction: Transaction): Option[C[T]] = {
      val pulses = value.flatMap(_.pulse(transaction))
      if (value.isEmpty) None else Some(pulses)
    }
  }
  override def ===(other: Signal[_]): Signal[Boolean] = other.map(_ == value)
}

object Val {
  def apply[A](value: A) = new Val(value)
}