package dctm.vars

import dctm.locks.TransactionReentrantReadWriteLock
import dctm.commit.Committable
import dctm.commit.Committable
import dctm.locks.TransactionLock

class TransactionalVariable[A, T](initialValue: A) extends TransactionalAccessControl[T] {
  private var transientValue = initialValue
  private var rollbackValue = initialValue
  
  override def writeCommit(transaction: T) {
    rollbackValue = transientValue;
  }
  override def writeRollback(transaction: T) {
    transientValue = rollbackValue;
  }

  def get()(implicit t: TransactionExecutionContext[T]) = {
    bindReadLockOrFail();
    transientValue;
  }

  def set(value: A)(implicit t: TransactionExecutionContext[T]) = {
    bindWriteLockOrFail();
    val previous = transientValue;
    transientValue = value;
    previous
  }

  def transform(op: A => A)(implicit t: TransactionExecutionContext[T]) = {
    bindWriteLockOrFail();
    val previous = transientValue;
    transientValue = op(transientValue)
    (previous, transientValue);
  }

  def lastCommitted() = rollbackValue
}