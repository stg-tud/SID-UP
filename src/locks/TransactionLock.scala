package locks

trait TransactionLock[A] {
//  def lockOrWait(tid: A)
  def lockOrFail(tid : A) : Boolean
  def release(tid: A)
}