package reactive.mutex

import reactive.events.EventStream
import reactive.events.impl.DependentEventStreamImpl
import reactive.impl.SingleDependentReactive
import reactive.Transaction

class LockEventStream[A](val dependency: EventStream[A], lock: TransactionLock) extends DependentEventStreamImpl[A] with SingleDependentReactive {
  var isLocked = false
  protected def reevaluate(transaction: Transaction): Option[A] = {
    isLocked = true
    lock.acquire(transaction.uuid)
    dependency.pulse(transaction)
  }

  override protected[reactive] def doPulse(transaction: Transaction, sourceDependenciesChanged: Boolean, pulse: Option[A]) {
    super.doPulse(transaction, sourceDependenciesChanged, pulse)
    if (isLocked) {
      isLocked = false
      lock.release(transaction.uuid)
    }
  }
}
