package reactive
package events

import reactive.signals.Val
import reactive.signals.Signal

@SerialVersionUID(123483712387L)
object NothingEventStream extends EventStream[Nothing] with ReactiveConstant[Nothing, Nothing] with Serializable {
  override def hold[B >: Nothing](initialValue: B): Signal[B] = new Val(initialValue)
  override def map[B](op: Nothing => B): EventStream[B] = this
  override def mapOption[B](op: Nothing => Option[B]): EventStream[B] = this
  override def collect[B](op: PartialFunction[Nothing,B]): EventStream[B] = this
  override def merge[B >: Nothing](streams: EventStream[B]*): EventStream[B] = if (streams.length == 1) streams.head else streams.head.merge(streams.tail: _*)
  override def fold[B](initialValue: B)(op: (B, Nothing) => B): Signal[B] = new Val(initialValue)
  override val log: Signal[List[Nothing]] = new Val(List[Nothing]())
  override def filter(op: Nothing => Boolean): EventStream[Nothing] = this
  override def deOption[B](implicit evidence: Nothing <:< Option[B]): EventStream[B] = this
}
