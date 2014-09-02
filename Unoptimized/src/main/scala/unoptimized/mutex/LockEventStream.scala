package unoptimized.mutex

import unoptimized.events.EventStream
import unoptimized.events.impl.DependentEventStreamImpl
import unoptimized.impl.SingleDependentReactive
import unoptimized.Transaction

class LockEventStream[A](val dependency: EventStream[A], override protected val lock: TransactionLock) extends DependentEventStreamImpl[A] with SingleDependentReactive with LockingReactive[A, A] {
  protected def reevaluate(transaction: Transaction): Option[A] = {
    dependency.pulse(transaction)
  }
}
