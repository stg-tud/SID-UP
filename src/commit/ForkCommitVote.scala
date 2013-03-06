package commit

class ForkCommitVote[-A](commitVotes: Iterable[CommitVote[A]]) extends CommitVote[A] {
  override def reigsterCommittable(transaction: A, committable: Committable[A]) {
    commitVotes.foreach { _.registerCommitable(transaction, committable) }
  }
  override def yes(transaction: A) {
    commitVotes.foreach { _.yes(transaction) }
  }
  override def no(transaction: A) {
    commitVotes.foreach { _.no(transaction) }
  }
}