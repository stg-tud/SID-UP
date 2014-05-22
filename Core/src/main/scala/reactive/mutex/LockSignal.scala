package reactive.mutex

import reactive.signals.Signal
import reactive.signals.impl.DependentSignalImpl
import reactive.impl.SingleDependentReactive
import reactive.Transaction

class LockSignal[A](override val dependency: Signal[A], lock: TransactionLock) extends DependentSignalImpl[A] with SingleDependentReactive {
  var isLocked = false
  protected def reevaluateValue(transaction: Transaction): A = {
    if (transaction != null) {
      isLocked = true
      lock.acquire(transaction.uuid)
    }
    dependency.value(transaction)
  }

  override protected[reactive] def doPulse(transaction: Transaction, sourceDependenciesChanged: Boolean, pulse: Option[A]) {
    super.doPulse(transaction, sourceDependenciesChanged, pulse)
    if (isLocked) {
      isLocked = false
      lock.release(transaction.uuid)
    }
  }

}
