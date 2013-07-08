package reactive.signals.impl

import reactive.Transaction
import reactive.impl.DependentReactive

trait DependentSignalImpl[A] extends SignalImpl[A] with DependentReactive[A] {
  protected var value = reevaluate(null)
  override def now = value
  // TODO this should respect the transaction stuff..
  override def value(transaction: Transaction) = value
  protected def reevaluate(transaction: Transaction): A
  protected override def calculatePulse(transaction: Transaction): Option[A] = {
    val newValue = reevaluate(transaction)
    if (newValue == value) {
      None
    } else {
      value = newValue
      Some(newValue)
    }
  }
}