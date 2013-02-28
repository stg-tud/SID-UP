package commit

class ForkCommitVote(commitVotes: Iterable[CommitVote]) extends CommitVote {
  override def yes {
    commitVotes.foreach { _.yes }
  }
  override def no {
    commitVotes.foreach { _.no }
  }
}