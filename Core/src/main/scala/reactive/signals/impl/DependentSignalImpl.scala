package reactive.signals.impl

import reactive.Transaction
import reactive.impl.DependentReactive
import scala.concurrent.stm.Ref
import scala.concurrent.stm.InTxn

trait DependentSignalImpl[A] extends SignalImpl[A] with DependentReactive[A] {

  protected def reevaluateValue(tx: InTxn): A

  override protected val value= Ref(reevaluateValue(null))

  protected override def reevaluate(tx: InTxn): Option[A] = {
    updateValue(tx, reevaluateValue(tx))
  }
}
