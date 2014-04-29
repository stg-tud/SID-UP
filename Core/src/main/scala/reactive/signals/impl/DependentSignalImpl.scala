package reactive.signals.impl

import reactive.Transaction
import reactive.impl.DependentReactive
import scala.concurrent.stm.Ref
import scala.concurrent.stm.atomic


trait DependentSignalImpl[A] extends SignalImpl[A] with DependentReactive[A] {

  protected def reevaluateValue(transaction: Transaction): A

  private val _value = Ref(reevaluateValue(null))

  def now = atomic { tx => _value()(tx) }

  override def value(transaction: Transaction) = now

  protected override def reevaluate(transaction: Transaction): Option[A] = atomic { tx =>
    val newValue = reevaluateValue(transaction)
    if (newValue == now) {
      None
    }
    else {
      _value.set(newValue)(tx)
      Some(newValue)
    }
  }
}
