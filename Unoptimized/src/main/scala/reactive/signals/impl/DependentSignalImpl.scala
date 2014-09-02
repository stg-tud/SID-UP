package reactive.signals.impl

import reactive.Transaction
import reactive.impl.DependentReactive

trait DependentSignalImpl[A] extends SignalImpl[A] with DependentReactive[A] {

  protected def reevaluateValue(transaction: Transaction): A

  private var _value = reevaluateValue(null)

  def now = _value

  override def value(transaction: Transaction) = _value

  protected override def reevaluate(transaction: Transaction): Option[A] = {
    val newValue = reevaluateValue(transaction)
    if (newValue == now) {
      None
    } else {
      _value = newValue
      Some(newValue)
    }
  }
}
