package commit

class DelegateCountdownAggregateCommitVote(pending: Int, delegate: CommitVote) extends CountdownAggregateCommitVote(pending) {
  override def success {
    delegate.yes
  }

  override def failure {
    delegate.no
  }
}