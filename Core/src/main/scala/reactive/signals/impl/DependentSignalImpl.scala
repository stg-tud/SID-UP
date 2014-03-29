package reactive.signals.impl

import reactive.Transaction
import reactive.impl.DependentReactive
import scala.concurrent.stm._

trait DependentSignalImpl[A] extends SignalImpl[A] with DependentReactive[A] {

  protected def reevaluateValue(transaction: Transaction): A

  //TODO: using null here was never a good idea, and by now it actually crashes
  private val _value = Ref(reevaluateValue(null))

  override protected def setValue(value: A)(implicit tx: InTxn): Unit = _value() = value

  def now = atomic { tx => _value()(tx) }

  protected override def reevaluate(transaction: Transaction): Option[A] = {
    val newValue = reevaluateValue(transaction)
    if (newValue == now) {
      None
    } else {
      Some(newValue)
    }
  }



}
