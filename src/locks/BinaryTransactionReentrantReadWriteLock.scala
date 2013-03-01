package locks

import scala.collection.mutable

// binary as in doesn't keep count, lock-lock-unlock means it's unlocked.
class BinaryTransactionReentrantReadWriteLock[A] {
  private val lock = new Object();
  private val readers = mutable.Set[A]()
  private var writer: Option[A] = None;
  def readLockOrFail(tid: A) = {
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

  def writeLockOrFail(tid: A) = {
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