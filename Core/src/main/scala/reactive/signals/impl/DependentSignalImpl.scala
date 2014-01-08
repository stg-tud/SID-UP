package reactive
package signals
package impl

import reactive.impl.DependentReactive

trait DependentSignalImpl[A] extends SignalImpl[A] with DependentReactive[A, Reactive.IDENTITY, Reactive.IDENTITY, Reactive.IDENTITY, Signal] {
  protected override def reevaluate(transaction: Transaction): Option[(A, A)] = {
    val newValue = reevaluateValue(transaction)
    if (newValue == now) {
      None
    } else {
      Some((newValue, newValue))
    }
  }
}