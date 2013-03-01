package commit

@remote trait Committable[A] {
  def commit(tid : A)
  def rollback(tid : A)
}