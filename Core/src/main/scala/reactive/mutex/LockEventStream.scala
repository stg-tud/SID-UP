package reactive.mutex

import reactive.events.EventStream
import reactive.events.impl.DependentEventStreamImpl
import reactive.impl.SingleDependentReactive
import reactive.Transaction

class LockEventStream[A](val dependency: EventStream[A], override protected val lock: TransactionLock) extends DependentEventStreamImpl[A] with SingleDependentReactive with LockingReactive[A, A] {
  protected def reevaluate(transaction: Transaction): Option[A] = {
    dependency.pulse(transaction)
  }
}
