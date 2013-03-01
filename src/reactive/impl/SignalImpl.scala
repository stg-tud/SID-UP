package reactive.impl

import util.Util.nullSafeEqual
import reactive.Signal
import reactive.EventStream
import reactive.Transaction
import reactive.Reactive
import commit.CommitVote
import remote.RemoteReactiveDependant
import util.Multiset
import java.util.UUID

abstract class SignalImpl[A](name: String, private var currentValue: A) extends ReactiveImpl[A](name) with Signal[A] {
  signal =>
  override def now = currentValue

  private var currentTransaction: Transaction = _
  private var newValue: A = _
  private var changed: Boolean = _
  def prepareCommit(transaction: Transaction, commitVotes: Iterable[CommitVote[Transaction]], calculateNewValue: A => A) {
    if (lock.writeLock.lockOrFail(transaction)) {
      currentTransaction = transaction;
      newValue = calculateNewValue(currentValue);
      changed = !nullSafeEqual(currentValue, newValue)
      if (changed) {
        notifyDependants(transaction, commitVotes, (newValue, changed));
      } else {
        lock.writeLock.release(transaction);
        commitVotes.foreach { _.unaffected() }
      }
    } else {
      commitVotes.foreach { _.no }
    }
  }

  override val changes: EventStream[A] = new EventStream[A] {
    override val name = signal.name + ".changes"
    override def observe(obs: A => Unit) = signal.observe(obs)
    override def unobserve(obs: A => Unit) = signal.unobserve(obs)
    override def addDependant(dependant: RemoteReactiveDependant[A]) { signal.addDependant(dependant) }
    override def removeDependant(dependant: RemoteReactiveDependant[A]) { signal.addDependant(dependant) }
    override def hold[B >: A](initialValue: B): Signal[B] = if (nullSafeEqual(initialValue, currentValue)) signal else new HoldSignal(this, initialValue);
    override def map[B](op: A => B): EventStream[B] = new MappedEventStream(this, op);
    override def merge[B >: A](streams: EventStream[B]*): EventStream[B] = new MergeStream((this +: streams): _*);
    override def fold[B](initialValue: B)(op: (B, A) => B): Signal[B] = new FoldSignal(initialValue, this, op);
    override def log = fold(List[A]())((list, elem) => list :+ elem)
    override def filter(op: A => Boolean): EventStream[A] = new FilteredEventStream(this, op);
  }
  override def map[B](op: A => B): Signal[B] = changes.map(op).hold(op(now))
  override def rmap[B](op: A => Signal[B]): Signal[B] = map(op).flatten
  override def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B] = new FlattenSignal(this.asInstanceOf[Signal[Signal[B]]]);
  override def log = changes.fold(List(currentValue))((list, elem) => list :+ elem);
  override def snapshot(when: EventStream[_]): Signal[A] = new SnapshotSignal(this, when);
}
