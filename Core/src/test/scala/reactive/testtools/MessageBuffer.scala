package reactive.testtools

import reactive.signals.impl.SignalImpl
import reactive.signals.Signal
import scala.collection.mutable
import reactive.Transaction
import reactive.impl.SingleDependentReactive
import reactive.signals.impl.DependentSignalImpl
import scala.concurrent.stm.InTxn


class MessageBuffer[A](override val dependency: Signal[A], tx: InTxn) extends SingleDependentReactive(tx) with DependentSignalImpl[A] {

  val messages = mutable.MutableList[(Transaction, Boolean, Boolean)]()

  override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean) {
    messages.synchronized {
      messages += ((transaction, sourceDependenciesChanged, pulsed))
    }
  }

  def releaseQueue() {
    messages.synchronized {
      val release = messages.toList
      messages.clear()
      release
    }.foreach { case (transaction, sourceDependenciesChanged, pulsed) =>
      doReevaluation(transaction, sourceDependenciesChanged, pulsed)
    }
  }

  override protected def reevaluateValue(tx: InTxn): A = dependency.now(tx)
}
