package commit

@remote trait CommittableRegistry[-A] {
  def registerCommitable(transaction : A, committable: Committable[A])
}