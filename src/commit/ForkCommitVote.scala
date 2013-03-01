package commit

class ForkCommitVote[A](commitVotes: Iterable[CommitVote[A]]) extends CommitVote[A] {
  override def reigsterCommittable(committable : Committable[A]) {
    commitVotes.foreach { _.registerCommitable(committable) }
  }
  override def yes() {
    commitVotes.foreach { _.yes() }
  }
  override def no() {
    commitVotes.foreach { _.no() }
  }
}