package reactive.mutex

import reactive.events.EventStream
import reactive.events.impl.DependentEventStreamImpl
import reactive.impl.SingleDependentReactive
import reactive.Transaction

class LockEventStream[A](val dependency: EventStream[A], lock: TransactionLock) extends DependentEventStreamImpl[A] with SingleDependentReactive {
  protected def reevaluate(transaction: Transaction): Option[A] = {
    dependency.pulse(transaction)
  }

  override protected[reactive] def doPulse(transaction: Transaction, sourceDependenciesChanged: Boolean, pulse: Option[A]) {
    super.doPulse(transaction, sourceDependenciesChanged, pulse)
    lock.acquire(transaction.uuid)
    lock.release(transaction.uuid)
  }
}
