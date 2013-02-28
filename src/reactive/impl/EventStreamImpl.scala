package reactive.impl

import reactive.EventStream
import reactive.Transaction
import reactive.Signal
import commit.CommitVote

abstract class EventStreamImpl[A](name: String) extends ReactiveImpl[A](name) with EventStream[A] {
  private var currentTransaction: Transaction = _
  private var currentEvent: A = _
  def prepareCommit(transaction: Transaction, commitVotes: Iterable[CommitVote], event: A) {
    if (lock.writeLock.lockOrFail(transaction)) {
      currentTransaction = transaction;
      currentEvent = event
      prepareDependants(transaction, commitVotes, event);
    } else {
      commitVotes.foreach { _.no }
    }
  }

  override def commit {
    commitDependants()
    notifyObservers(currentTransaction, currentEvent)
    lock.writeLock.release(currentTransaction)
  }

  override def rollback {
    rollbackDependants()
    lock.writeLock.release(currentTransaction)
  }

  override def hold[B >: A](initialValue: B): Signal[B] = new HoldSignal(this, initialValue);
  override def map[B](op: A => B): EventStream[B] = new MappedEventStream(this, op);
  override def merge[B >: A](streams: EventStream[B]*): EventStream[B] = new MergeStream((this +: streams): _*);
  override def fold[B](initialValue: B)(op: (B, A) => B): Signal[B] = new FoldSignal(initialValue, this, op);
  override def log = fold(List[A]())((list, elem) => list :+ elem)
  override def filter(op: A => Boolean): EventStream[A] = new FilteredEventStream(this, op);
}