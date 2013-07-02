package reactive
package signals

import reactive.Transaction
import reactive.events.EventStream
import reactive.events.NothingEventStream
import util.Update

class Val[A](value: A) extends Signal[A] with ReactiveConstant[A, A, Update[A]] {
  override val now = value
  override def apply()(implicit t: Transaction) = value
  override def transientPulse(t : Transaction) = None
  override lazy val log = new Val(List(value))
  override val changes: EventStream[A] = NothingEventStream
  override def map[B](op: A => B): Signal[B] = new Val(op(value))
  override def flatMap[B](op: A => Signal[B]): Signal[B] = op(value)
  override def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B] = value.asInstanceOf[Signal[B]]
  override def snapshot(when: EventStream[_]): Signal[A] = this
}
