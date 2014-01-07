package reactive.testtools

import reactive.signals.impl.SignalImpl
import reactive.signals.Signal
import scala.collection.mutable
import scala.util.Random
import util.TicketAccumulator
import reactive.Transaction
import reactive.Reactive
import reactive.impl.SingleDependentReactive
import reactive.signals.impl.DependentSignalImpl


class MessageBuffer[A](override val dependency: Signal[A]) extends SignalImpl[A] with DependentSignalImpl[A] with SingleDependentReactive {

  val messages = mutable.MutableList[(Transaction, Boolean, Boolean)]()

  override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean) {
     messages.synchronized {
       messages += ( (transaction, sourceDependenciesChanged, pulsed) );
     }
  }

  def releaseQueue() {
    messages.synchronized {
     val release = messages.toList;
     messages.clear()
     release
    }.foreach { case (transaction, sourceDependenciesChanged, pulsed) =>
      doReevaluation(transaction, sourceDependenciesChanged, pulsed)
    }
  }

  override protected def reevaluateValue(transaction: Transaction): A = dependency.value(transaction)
}
