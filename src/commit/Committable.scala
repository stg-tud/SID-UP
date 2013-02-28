package commit

@remote trait Committable {
  def commit
  def rollback
}