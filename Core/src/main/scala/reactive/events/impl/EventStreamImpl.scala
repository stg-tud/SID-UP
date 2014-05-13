package reactive
package events
package impl

import reactive.impl.ReactiveImpl
import reactive.signals.Signal
import reactive.signals.impl.FoldSignal
import scala.concurrent.stm._

trait EventStreamImpl[A] extends ReactiveImpl[A, A] with EventStream[A] {
  override def hold[B >: A](initialValue: B)(implicit inTxn: InTxn): Signal[B] = fold(initialValue) { (_, value) => value }
  override def map[B](op: A => B)(implicit inTxn: InTxn): EventStream[B] = new MappedEventStream(this, op, inTxn);
  override def collect[B](op: PartialFunction[A, B])(implicit inTxn: InTxn): EventStream[B] = new PartiallyMappedEventStream(this, op, inTxn);
  override def merge[B >: A](streams: EventStream[B]*)(implicit inTxn: InTxn): EventStream[B] = new MergeStream(this :: streams.toList, inTxn);
  override def fold[B](initialValue: B)(op: (B, A) => B)(implicit inTxn: InTxn): Signal[B] = new FoldSignal(initialValue, this, op, inTxn);
  override def log(implicit inTxn: InTxn) = fold(List[A]())((list, elem) => list :+ elem)
  override def filter(op: A => Boolean)(implicit inTxn: InTxn): EventStream[A] = new FilteredEventStream(this, op, inTxn);

  protected override def getObserverValue(transaction: Transaction, pulseValue: A) = pulseValue
}

object EventStreamImpl {
  trait ViewImpl[A] extends ReactiveImpl.ViewImpl[A] with EventStream.View[A] {
    override protected val impl: EventStreamImpl[A]
    override def hold[B >: A](initialValue: B): Signal[B] = atomic { impl.hold(initialValue)(_) }
    override def map[B](op: A => B): EventStream[B] = atomic { impl.map(op)(_) }
    override def collect[B](op: PartialFunction[A, B]): EventStream[B] = atomic { impl.collect(op)(_) }
    override def merge[B >: A](streams: EventStream[B]*): EventStream[B] = atomic { impl.merge(streams: _*)(_) }
    override def fold[B](initialValue: B)(op: (B, A) => B): Signal[B] = atomic { impl.fold(initialValue)(op)(_) }
    override def filter(op: A => Boolean): EventStream[A] = atomic { impl.filter(op)(_) }
  }
}
