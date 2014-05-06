package reactive.signals.impl

import reactive.Transaction
import reactive.impl.DependentReactive
import scala.concurrent.stm._

trait DependentSignalImpl[A] extends SignalImpl[A] with DependentReactive[A] {

  protected def reevaluateValue(transaction: Transaction): A

  //TODO: using null here was never a good idea, and by now it actually crashes
  //crashing is fixed for now, null is still bad
  //TODO: what about using an empty transaction?
  private val _value = Ref(reevaluateValue(new Transaction(Set())))

  override protected def setValue(value: A)(implicit tx: InTxn): Unit = _value() = value

  def now = _value.single.get

  protected override def reevaluate(transaction: Transaction): Option[A] = {
    val newValue = reevaluateValue(transaction)
    if (newValue == now) {
      None
    } else {
      Some(newValue)
    }
  }



}
