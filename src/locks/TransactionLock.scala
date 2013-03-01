package locks

trait TransactionLock[A] {
//  def lockOrWait(tid: A)
  def isHeld(tid : A) : Boolean
  def lockOrFail(tid : A) : Boolean
  def release(tid: A)
}