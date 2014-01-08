package reactive
package signals

import reactive.events.EventStream
import reactive.events.NothingEventStream
import reactive.impl.mirroring.ReactiveMirror

class Val[A](val value: A) extends Signal[A] with ReactiveConstant[A, A, A, Val[A]] {
  self =>
  override val now = value
  override def value(t: Transaction) = value
  override lazy val log = new Val(List(value))
  override val changes: EventStream[A] = NothingEventStream
  override def map[B](op: A => B): Signal[B] = new Val(op(value))
  override def flatMap[B](op: A => Signal[B]): Signal[B] = op(value)
  override def flatten[R <: Reactive[_, _, _, R]](implicit evidence: A <:< R): R = value
  override def snapshot(when: EventStream[_]): Signal[A] = this
  override def pulse(when: EventStream[_]): EventStream[A] = when.map { _ => value }
  override def mirror = new ReactiveMirror[A, A, A, Val[A]]{
    override def mirror = self
  }
}

object Val {
  def apply[A](value: A) = new Val(value)
}
