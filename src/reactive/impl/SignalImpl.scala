package reactive
package impl

import Reactive._
import util.Multiset
import java.util.UUID
import remote.RemoteReactiveDependant
import dctm.vars.TransactionExecutionContext
import dctm.vars.TransactionExecutionContext
import dctm.vars.TransactionalVariable
import dctm.vars.TransactionExecutor
import util.Util

abstract class SignalImpl[A](name: String, initialValue: A) extends ReactiveImpl[A](name) with Signal[A] {
  signal =>

  private val value = new TransactionalVariable[A, Transaction](initialValue);
  override def now = TransactionBuilder.retryUntilSuccess(this()(_))
  override def apply()(implicit t: Txn): A = {
    value.get()
  }

  protected def notifyDependants(sourceDependenciesDiff: Multiset[UUID], maybeValue: Option[A])(implicit t: Txn) {
    if (maybeValue.isDefined) {
      val oldValue = value.set(maybeValue.get)
      super.notifyDependants(sourceDependenciesDiff, maybeValue.filterNot(Util.nullSafeEqual(oldValue, _)));
    } else {
      super.notifyDependants(sourceDependenciesDiff, None);
    }
  }

  override val changes: EventStream[A] = new EventStream[A] {
    override val name = signal.name + ".changes"
    override def observe(obs: A => Unit) = signal.observe(obs)
    override def unobserve(obs: A => Unit) = signal.unobserve(obs)
    override def addDependant(dependant: RemoteReactiveDependant[A])(implicit t: Txn) { signal.addDependant(dependant) }
    override def removeDependant(dependant: RemoteReactiveDependant[A])(implicit t: Txn) { signal.addDependant(dependant) }
    override def hold[B >: A](initialValue: B)(t: Txn): Signal[B] = if (Util.nullSafeEqual(initialValue, now)) signal else new HoldSignal(this, initialValue, t);
    override def map[B](op: A => B)(t: Txn): EventStream[B] = new MappedEventStream(this, op, t);
    override def merge[B >: A](streams: EventStream[B]*)(t: Txn): EventStream[B] = new MergeStream((this +: streams): _*);
    override def fold[B](initialValue: B)(op: (B, A) => B)(t: Txn): Signal[B] = new FoldSignal(initialValue, this, op, t);
    override def log(t: Txn) = fold(List[A]())((list, elem) => list :+ elem)(t)
    override def filter(op: A => Boolean)(t : Txn): EventStream[A] = new FilteredEventStream(this, op, t);
  }
  override def map[B](op: A => B)(t: Txn): Signal[B] = changes.map(op)(t).hold(op(now))(t)
  override def rmap[B](op: A => Signal[B])(t: Txn): Signal[B] = map(op)(t).flatten
  override def flatten[B](implicit evidence: A <:< Signal[B], t: Txn): Signal[B] = new FlattenSignal(this.asInstanceOf[Signal[Signal[B]]]);
  override def log(t: Txn) = {
    def inTxn(t: Txn) = new FoldSignal(List(this()(t)), changes, ((list: List[A], elem: A) => list :+ elem), t);
    if (t == null) {
      TransactionBuilder.retryUntilSuccess(inTxn)
    } else {
      inTxn(t);
    }
  }
  override def snapshot(when: EventStream[_]): Signal[A] = new SnapshotSignal(this, when);
}
