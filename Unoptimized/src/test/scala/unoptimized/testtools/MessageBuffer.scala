package unoptimized.testtools

import unoptimized.signals.impl.SignalImpl
import unoptimized.signals.Signal
import scala.collection.mutable
import unoptimized.Transaction
import unoptimized.impl.SingleDependentReactive
import unoptimized.signals.impl.DependentSignalImpl


class MessageBuffer[A](override val dependency: Signal[A]) extends SignalImpl[A] with DependentSignalImpl[A] with SingleDependentReactive {

  val messages = mutable.MutableList[(Transaction, Boolean, Boolean)]()

  override def ping(transaction: Transaction): Unit = {
    messages.synchronized {
      messages += ((transaction, dependency.sourceDependenciesChanged(transaction), dependency.pulse(transaction).isDefined))
    }
  }

  def releaseQueue(): Unit = {
    messages.synchronized {
      val release = messages.toList
      messages.clear()
      release
    }.foreach { case (transaction, sourceDependenciesChanged, pulsed) =>
      doReevaluation(transaction, sourceDependenciesChanged, pulsed)
    }
  }

  override protected def reevaluateValue(transaction: Transaction): A = dependency.value(transaction)
}
