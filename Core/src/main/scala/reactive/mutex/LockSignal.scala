package reactive.mutex

import reactive.signals.Signal
import reactive.signals.impl.DependentSignalImpl
import reactive.impl.SingleDependentReactive
import reactive.Transaction

class LockSignal[A](override val dependency: Signal[A], override protected val lock: TransactionLock) extends DependentSignalImpl[A] with SingleDependentReactive with LockingReactive[A, A] {
  protected def reevaluateValue(transaction: Transaction): A = {
    dependency.value(transaction)
  }
}
