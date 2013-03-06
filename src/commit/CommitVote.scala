package commit

@remote trait CommitVote[-A] extends CommittableRegistry[A] {
  def yes(transaction : A)
  def no(transaction : A)
}