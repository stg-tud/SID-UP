package reactive.impl

import reactive.EventStream
import reactive.Transaction
import remote.RemoteReactiveDependant
import commit.CommitVote
import util.Multiset
import java.util.UUID

class FoldSignal[A, B](initialValue: A, source: EventStream[B], op: (A, B) => A) extends SignalImpl[A]("fold(" + source.name + ")", initialValue) with RemoteReactiveDependant[B] {
  source.addDependant(this)

  override def notify(transaction: Transaction, commitVote: CommitVote[Transaction], sourceDependenciesDiff : Multiset[UUID], maybeValue: Option[B]) {
    if(maybeValue.isDefined) {
      lock.withReadLockOrVoteNo(transaction, commitVote) {
        commitVote.registerCommitable(this)
        notifyDependants(transaction, commitVote, sourceDependenciesDiff, maybeValue.map{ op(now, _) });
      }
    } else {
      super.notifyDependants(transaction, commitVote, sourceDependenciesDiff, None);
    }
  }
}