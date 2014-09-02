package unoptimized.mutex

import unoptimized.Transaction
import unoptimized.impl.ReactiveImpl

trait LockingReactive[O, P] extends ReactiveImpl[O, P] {
  protected val lock: TransactionLock
  override protected[unoptimized] def doPulse(transaction: Transaction, sourceDependenciesChanged: Boolean, pulse: Option[P]): Unit = {
    lock.acquire(transaction.uuid)
    super.doPulse(transaction, sourceDependenciesChanged, pulse)
    lock.release(transaction.uuid)
  }
}