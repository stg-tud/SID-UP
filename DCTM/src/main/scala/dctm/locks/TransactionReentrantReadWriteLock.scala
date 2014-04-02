package dctm.locks

import scala.collection.mutable

class TransactionReentrantReadWriteLock[A] {
  private val lock = new Object();
  private val readers = mutable.Map[A, Int]()
  private var writer: A = _;
  private var writerCount = 0;
  val readLock = new TransactionLock[A] {
    override def tryLock(tid: A) = {
      lock.synchronized {
        readers.get(tid) match {
          case Some(count) =>
            readers += tid -> (count + 1)
            true;
          case None =>
            if (writerCount == 0 || writer.equals(tid)) {
              readers += tid -> 1
              true
            } else {
              false
            }
        }
      }
    }

    override def isHeld(tid: A) = {
      lock.synchronized {
        readers.contains(tid)
      }
    }

    override def isFree() = {
      lock.synchronized {
        readers.isEmpty
      }
    }

    override def release(tid: A) {
      lock.synchronized {
        assert(isHeld(tid));
        val count = readers(tid) - 1
        if (count == 0) {
          readers -= tid
        } else {
          readers += tid -> count
        }
      }
    }
  }

  val writeLock = new TransactionLock[A] {
    override def tryLock(tid: A) = {
      lock.synchronized {
        if (writerCount == 0) {
          if (readers.isEmpty || (readers.size == 1 && readers.contains(tid))) {
            writer = tid;
            writerCount = 1;
            true
          } else {
            false
          }
        } else if (writer.equals(tid)) {
          writerCount += 1;
          true;
        } else {
          false
        }
      }
    }

    override def isHeld(tid: A) = {
      lock.synchronized {
        writerCount > 0 && writer.equals(tid);
      }
    }

    override def isFree() = {
      lock.synchronized {
        writerCount == 0
      }
    }

    override def release(tid: A) {
      assert(isHeld(tid));
      writerCount -= 1
    }
  }

  def isFree() = {
    lock.synchronized {
      readLock.isFree && writeLock.isFree
    }
  }
}