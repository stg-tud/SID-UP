package commit

@remote trait CommitVote[A] {
  def registerCommitable(committable: Committable[A])
  def yes()
  def no()
}