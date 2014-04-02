package dctm.commit

@remote trait CommittableRegistry[A] {
  def registerCommittable(transaction : A, committable: Committable[A])
}