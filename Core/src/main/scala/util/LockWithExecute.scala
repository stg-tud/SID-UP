package util

import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.concurrent.locks.Lock

class LockWithExecute(lock: ReentrantReadWriteLock) {
  def readLocked[A](op: => A): A = {
    underLock(lock.readLock, op)
  }
  def writeLocked[A](op: => A): A = {
    underLock(lock.writeLock, op)
  }
  private def underLock[A](lock: Lock, op: => A): A = {
    lock.lock();
    try {
      op
    } finally {
      lock.unlock();
    }
  }
}

object LockWithExecute {
  implicit def apply(lock: ReentrantReadWriteLock) = new LockWithExecute(lock)
}