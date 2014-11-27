package reactive
package signals

import reactive.events.EventStream
import reactive.events.NothingEventStream
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds 
import reactive.events.TransposeEventStream

class Val[A](val value: A) extends Signal[A] with ReactiveConstant[A, A] {
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
  override def transposeS[T, C[B] <: Traversable[B]](implicit evidence: A <:< C[Signal[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]]) = new TransposeSignal[T, C](/*this.map(evidence)*/ this.asInstanceOf[Signal[C[Signal[T]]]])
  override def transposeE[T, C[B] <: Traversable[B]](implicit evidence: A <:< C[EventStream[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]]) = new TransposeEventStream[T, C](/*this.map(evidence)*/ this.asInstanceOf[Signal[C[EventStream[T]]]])
}

object Val {
  def apply[A](value: A) = new Val(value)
}