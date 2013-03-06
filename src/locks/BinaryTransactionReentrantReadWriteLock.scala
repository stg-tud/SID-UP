package locks

import scala.collection.mutable
import commit.CommitVote

// binary as in doesn't keep count, lock-lock-unlock means it's unlocked.
class BinaryTransactionReentrantReadWriteLock[A](name : String = null) {
  private val lock = new Object();
  private val readers = mutable.Set[A]()
  private var writer: Option[A] = None;
  def tryReadLock(tid: A) = {
    lock.synchronized {
      if (readers.contains(tid)) {
        true
      } else if (writer.isEmpty || writer.get.equals(tid)) {
        readers += tid;
        true
      } else {
        false
      }
    }
  }

  def tryWriteLock(tid: A) = {
    lock.synchronized {
      if (writer.isDefined) {
        writer.get.equals(tid)
      } else if (readers.isEmpty) {
        writer = Some(tid)
        true
      } else if (readers.size == 1 && readers.head.equals(tid)) {
        readers -= tid;
        writer = Some(tid)
        true;
      } else {
        false
      }
    }
  }

  def readLockOrFail(tid : A) {
   if(!tryReadLock(tid)) {
     lockAcquisitionFailure();
   }
  }
  
  def withReadLockOrVoteNo(tid : A, commitVote : CommitVote[A])(op : => Unit) {
    if(tryReadLock(tid)) {
      op;
    } else {
      commitVote.no(tid)
    }
  }
  
  def writeLockOrFail(tid : A) {
    if(!tryWriteLock(tid)) {
      lockAcquisitionFailure();
    }
  }
  
  def withWriteLockOrVoteNo(tid : A, commitVote : CommitVote[A])(op : => Unit) {
    if(tryWriteLock(tid)) {
      op;
    } else {
      commitVote.no(tid)
    }
  }
  
  private def lockAcquisitionFailure() = throw new LockAcquisitionFailure(name)
  
  def release(tid: A) {
    lock.synchronized {
      writer match {
        case Some(tid) =>
          writer = None
        case None =>
          readers -= tid;
        case _ =>
      }
    }
  }
}