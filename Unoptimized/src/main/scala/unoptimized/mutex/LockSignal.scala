package unoptimized.mutex

import unoptimized.signals.Signal
import unoptimized.signals.impl.DependentSignalImpl
import unoptimized.impl.SingleDependentReactive
import unoptimized.Transaction

class LockSignal[A](override val dependency: Signal[A], override protected val lock: TransactionLock) extends DependentSignalImpl[A] with SingleDependentReactive with LockingReactive[A, A] {
  protected def reevaluateValue(transaction: Transaction): A = {
    dependency.value(transaction)
  }
}
