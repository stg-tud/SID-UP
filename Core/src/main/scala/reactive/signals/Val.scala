package reactive
package signals

import reactive.events.{EventStream, NothingEventStream}

import scala.concurrent.stm._

case class Val[A](value: A) extends Signal[A] with ReactiveConstant[A, A] {
  impl =>

  override def now(implicit inTxn: InTxn) = single.now
  override def delta(implicit inTxn: InTxn): EventStream[(A, A)] = single.delta
  override def log(implicit inTxn: InTxn): Signal[List[A]] = single.log
  override def changes(implicit inTxn: InTxn): EventStream[A] = single.changes
  override def map[B](op: A => B)(implicit inTxn: InTxn): Signal[B] = single.map(op)
  override def tmap[B](op: (A, InTxn) => B)(implicit inTxn: InTxn): Signal[B] = new Val(op(value, inTxn))
  override def flatMap[B](op: A => Signal[B])(implicit inTxn: InTxn): Signal[B] = single.flatMap(op)
  override def tflatMap[B](op: (A, InTxn) => Signal[B])(implicit inTxn: InTxn): Signal[B] = op(value, inTxn)
  override def flatten[B](implicit evidence: A <:< Signal[B], inTxn: InTxn): Signal[B] = single.flatten
  override def snapshot(when: EventStream[_])(implicit inTxn: InTxn): Signal[A] = single.snapshot(when)
  override def pulse(when: EventStream[_])(implicit inTxn: InTxn): EventStream[A] = when.map { _ => value }(inTxn)

  override object single extends Signal.View[A] with ReactiveConstant.View[A] {
    override val now = value
    override val delta: EventStream[(A, A)] = NothingEventStream
    override lazy val log: Signal[List[A]] = new Val(List(value))
    override val changes: EventStream[A] = NothingEventStream
    override def map[B](op: A => B): Signal[B] = new Val(op(value))
    override def tmap[B](op: (A, InTxn) => B): Signal[B] = new Val(atomic { op(value, _) })
    override def flatMap[B](op: A => Signal[B]): Signal[B] = op(value)
    override def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B] = evidence(value)
    override def snapshot(when: EventStream[_]): Signal[A] = impl
    override def pulse(when: EventStream[_]): EventStream[A] = when.single.map { _ => value }
  }
}
