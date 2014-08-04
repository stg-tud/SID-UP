package reactive.signals.impl

import reactive.impl.DependentReactive

import scala.concurrent.stm.{InTxn, Ref}

trait DependentSignalImpl[A] extends SignalImpl[A] with DependentReactive[A] {
  self =>
  protected def reevaluateValue(tx: InTxn): A

  override protected val value = Ref(scala.concurrent.stm.atomic { reevaluateValue })

  protected override def reevaluate(tx: InTxn): Option[A] = {
    updateValue(tx, reevaluateValue(tx))
  }

  override object single extends {
    override protected val impl = self
  } with SignalImpl.ViewImpl[A] with DependentReactive.ViewImpl[A]
}
