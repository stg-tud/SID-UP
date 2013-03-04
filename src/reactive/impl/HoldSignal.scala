package reactive.impl

import reactive.EventStream
import reactive.Transaction
import remote.RemoteReactiveDependant
import commit.CommitVote
import util.Multiset
import java.util.UUID

class HoldSignal[A](override val changes: EventStream[A], initialValue: A) extends SignalImpl[A]("hold(" + changes.name + ")", initialValue) with RemoteReactiveDependant[A] {
  changes.addDependant(this);

  override def notify(transaction: Transaction, commitVote: CommitVote[Transaction], sourceDependencyDiff : Multiset[UUID], value: Option[A]) {
    notifyDependants(transaction, commitVote, sourceDependencyDiff, value);
  }
}