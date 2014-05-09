package reactive
package signals
package impl

import reactive.impl.ReactiveImpl
import reactive.events.EventStream
import reactive.events.impl.ChangesEventStream
import reactive.events.impl.DeltaEventStream
import reactive.events.impl.PulseEventStream
import scala.concurrent.stm._

trait SignalImpl[A] extends ReactiveImpl[A, A] with Signal[A] {
  impl =>
  protected val value: Ref[A]
  protected def updateValue(tx: InTxn, newValue: A): Option[A] = {
    if (value.swap(newValue)(tx) == newValue) {
      None
    } else {
      Some(newValue)
    }
  }

  override def now(implicit inTxn: InTxn) = value()
  override def changes(implicit inTxn: InTxn): EventStream[A] = new ChangesEventStream(this)
  override def map[B](op: A => B)(implicit inTxn: InTxn): Signal[B] = new MapSignal(this, op)
  override def delta(implicit inTxn: InTxn): EventStream[(A, A)] = new DeltaEventStream(this)
  override def flatMap[B](op: A => Signal[B])(implicit inTxn: InTxn): Signal[B] = map(op).flatten
  override def flatten[B](implicit evidence: A <:< Signal[B], inTxn: InTxn): Signal[B] = new FlattenSignal(this.asInstanceOf[Signal[Signal[B]]])
  override def snapshot(when: EventStream[_])(implicit inTxn: InTxn): Signal[A] = pulse(when).hold(now)
  override def pulse(when: EventStream[_])(implicit inTxn: InTxn): EventStream[A] = new PulseEventStream(this, when)
  override def log(implicit inTxn: InTxn) = new FoldSignal(List(now), changes, ((list: List[A], elem: A) => list :+ elem))

  protected override def getObserverValue(transaction: Transaction, pulseValue: A) = pulseValue
}

object SignalImpl {
  trait ViewImpl[A] extends ReactiveImpl.ViewImpl[A] with Signal.View[A] {
    override protected val impl: SignalImpl[A]
    override def now = impl.value.single.get
    override lazy val changes: EventStream[A] = atomic { impl.changes(_) }
    override lazy val delta = atomic { impl.delta(_) }
    override def map[B](op: A => B): Signal[B] = atomic { impl.map(op)(_) }
    override def flatMap[B](op: A => Signal[B]): Signal[B] = atomic { impl.flatMap(op)(_) }
    override def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B] = atomic { impl.flatten(evidence, _) }
    override def snapshot(when: EventStream[_]): Signal[A] = atomic { impl.snapshot(when)(_) }
    override def pulse(when: EventStream[_]): EventStream[A] = atomic { impl.pulse(when)(_) }
  }
}