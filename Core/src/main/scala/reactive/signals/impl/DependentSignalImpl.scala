package reactive.signals.impl

import reactive.Transaction
import reactive.impl.DependentReactive

trait DependentSignalImpl[A] extends SignalImpl[A] with DependentReactive[A, A] {
  protected override def reevaluate(transaction: Transaction): Option[(A, A)] = {
    val newValue = reevaluateValue(transaction)
    if (newValue == now) {
      None
    } else {
      Some((newValue, newValue))
    }
  }
}