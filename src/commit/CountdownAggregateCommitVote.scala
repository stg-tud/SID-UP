package commit

abstract class CountdownAggregateCommitVote(private var pending: Int) extends CommitVote {
  var failed = false
  override def yes = this.synchronized {
    pending -= 1;
    if (pending == 0) {
      success
    }
  }
  def success

  override def no = this.synchronized {
    if (!failed) {
      failed = true
      failure
    }
  }
  def failure
}