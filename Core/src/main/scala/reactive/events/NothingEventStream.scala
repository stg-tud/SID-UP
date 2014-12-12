package reactive
package events

import reactive.signals.{ Signal, Val }

import scala.concurrent.stm.InTxn

case object NothingEventStream extends EventStream[Nothing] with ReactiveConstant[Nothing, Nothing] {
  impl =>
  override def withName(name: String): this.type = this

  override def hold[B >: Nothing](initialValue: B): Signal[B] = new Val(initialValue)
  override def map[B](op: Nothing => B): EventStream[B] = this
  override def collect[B](op: PartialFunction[Nothing, B]): EventStream[B] = this
  override def merge[B >: Nothing](streams: EventStream[B]*): EventStream[B] = if (streams.length == 1) streams.head else streams.head.merge(streams.tail: _*)
  override def fold[B](initialValue: B)(op: (B, Nothing) => B): Signal[B] = new Val(initialValue)
  override val log: Signal[List[Nothing]] = new Val(List[Nothing]())
  override def filter(op: Nothing => Boolean): EventStream[Nothing] = this

  override object transactional extends EventStream.View[Nothing] with ReactiveConstant.View[Nothing] {
    override def hold[B >: Nothing](initialValue: B)(implicit inTxn: InTxn): Signal[B] = impl.hold(initialValue)
    override def map[B](op: Nothing => B)(implicit inTxn: InTxn): EventStream[B] = impl.map(op)
    override def collect[B](op: PartialFunction[Nothing, B])(implicit inTxn: InTxn): EventStream[B] = impl.collect(op)
    override def merge[B >: Nothing](streams: EventStream[B]*)(implicit inTxn: InTxn): EventStream[B] = if (streams.length == 1) streams.head else streams.head.transactional.merge(streams.tail: _*)(inTxn)
    override def fold[B](initialValue: B)(op: (B, Nothing) => B)(implicit inTxn: InTxn): Signal[B] = impl.fold(initialValue)(op)
    override def log(implicit inTxn: InTxn): Signal[List[Nothing]] = impl.log
    override def filter(op: Nothing => Boolean)(implicit inTxn: InTxn): EventStream[Nothing] = impl.filter(op)
  }
}
