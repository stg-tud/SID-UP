package dctm.vars

import dctm.locks.TransactionReentrantReadWriteLock
import dctm.commit.Committable
import dctm.commit.Committable
import dctm.locks.TransactionLock

class TransactionalAccessControl[T] {
  private val lock = new TransactionReentrantReadWriteLock[T]()

  def isFree = lock.isFree
  
  protected def writeCommit(transaction : T) {}
  protected def writeRollback(transaction : T) {}
  protected def readCommit(transaction : T) {}
  protected def readRollback(transaction : T) {}

  private val writeCommittable = new Committable[T] {
    override def commit(transaction: T) {
      writeCommit(transaction);
      lock.writeLock.release(transaction);
    }
    override def rollback(transaction: T) {
      writeRollback(transaction);
      lock.writeLock.release(transaction);
    }
  }
  
  private val readCommittable = new Committable[T] {
    override def commit(transaction: T) {
      lock.readLock.release(transaction);
    }
    override def rollback(transaction: T) {
      lock.readLock.release(transaction);
    }
  }

  protected def bindWriteLockOrFail()(implicit t: TransactionExecutionContext[T]) {
    lock.writeLock.lockOrFail(t.tid)
    t.commitVote.registerCommittable(t.tid, writeCommittable);
  }

  protected def bindReadLockOrFail()(implicit t: TransactionExecutionContext[T]): Unit = {
    lock.readLock.lockOrFail(t.tid)
    t.commitVote.registerCommittable(t.tid, readCommittable);
  }
}