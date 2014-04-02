package dctm.locks

import dctm.vars.TransactionExecutionContext
import dctm.commit.Committable

trait TransactionLock[A] {
  //  def lockOrWait(tid: A)
  def isHeld(tid: A): Boolean
  def tryLock(tid: A): Boolean
  @throws(classOf[LockAcquisitionFailure])
  def lockOrFail(tid: A) {
    if (!tryLock(tid)) {
      throw new LockAcquisitionFailure;
    }
  }
  def release(tid: A)
  def isFree() : Boolean
}