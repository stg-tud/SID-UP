package reactive.mutex

import reactive.Transaction
import reactive.impl.ReactiveImpl

abstract trait LockingReactive[O, P] extends ReactiveImpl[O, P] {
  protected val lock: TransactionLock
  override protected[reactive] abstract def doPulse(transaction: Transaction, sourceDependenciesChanged: Boolean, pulse: Option[P]) {
    lock.acquire(transaction.uuid)
    super.doPulse(transaction, sourceDependenciesChanged, pulse)
    lock.release(transaction.uuid)
  }
}