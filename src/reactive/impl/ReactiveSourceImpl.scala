package reactive.impl

import remote.RemoteReactiveDependant
import reactive.ReactiveSource
import commit.CommitVote
import reactive.Transaction
import util.Multiset

trait ReactiveSourceImpl[A] extends ReactiveSource[A] {
  self : ReactiveImpl[A] =>
  def prepareCommit(transaction: Transaction, commitVote : CommitVote[Transaction], value: A) {
    notifyDependants(transaction, commitVote, Multiset.empty, Some(value))
  }
}