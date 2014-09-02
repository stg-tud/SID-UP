package reactive.mutex

import java.util.UUID
import java.rmi.server.UnicastRemoteObject

@remote trait TransactionLock {
  def acquire(uuid: UUID): Unit
  def release(uuid: UUID): Unit
  def owner: Option[UUID]
}

class TransactionLockImpl extends UnicastRemoteObject with TransactionLock {
  private var counter = 0
  private var tid: Option[UUID] = None
  
  override def owner = tid

  private def isAcquiredBy(uuid: UUID) = {
    tid match {
      case Some(uuidAcquired) => uuid == uuidAcquired
      case None => false
    }
  }
  private def canAcquire(uuid: UUID) = {
    tid match {
      case Some(uuidAcquired) => uuid == uuidAcquired
      case None => true
    }
  }

  def acquire(uuid: UUID): Unit = synchronized {
    while (!canAcquire(uuid)) wait()
    tid = Option(uuid)
    counter += 1
  }

  def release(uuid: UUID): Unit = synchronized {
    if (isAcquiredBy(uuid)) {
      counter -= 1
      if (counter == 0) {
        tid = None
        notifyAll()
      }
    }
  }
}
