package commit
import reactive.Reactive

class CountdownAggregateCommitVote[-A](val tid: A, delegate: CommitVote[A], private var pendingVoteCount: Int) extends CommitVote[A] {
  private val lock = new Object;
  private var failed = false
  private var committables: List[Committable[A]] = Nil
  private val aggregateCommittable = new Committable[A] {
    override def commit(atid: A) {
      Reactive.executePooledForeach(committables) { _.commit(atid) }
      committables = Nil
    }
    override def rollback(atid: A) {
      Reactive.executePooledForeach(committables) { _.rollback(atid) }
      committables = Nil
    }
  }

  override def registerCommittable(committable: Committable[A]) {
    val invokeRollback = lock.synchronized {
      if (failed) {
        true
      } else {
        committables = committable :: committables;
        false
      }
    }
    if (invokeRollback) {
      committable.rollback(tid)
    }
  }

  override def yes(transaction : A) {
    lock.synchronized {
      pendingVoteCount -= 1;
      if (pendingVoteCount == 0) {
        // can report success inside synchronized block because no more votes will
        // be received that could be blocked from executing by the held lock
        if (!committables.isEmpty) {
          delegate.registerCommitable(transaction, aggregateCommittable)
        }
        delegate.yes(transaction);
      }
    }
  }

  override def no(transaction : A) {
    val invokeFailure = lock.synchronized {
      val invokeFailure = !failed;
      failed = true;
      invokeFailure
    }
    if (invokeFailure) {
      aggregateCommittable.rollback(tid)
      delegate.no(transaction)
    }
  }
}