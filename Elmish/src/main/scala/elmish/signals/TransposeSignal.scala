package elmish.signals

import elmish.impl.MultiDependentReactive
import elmish.{Reactive, Transaction}
import elmish.signals.impl.DependentSignalImpl

/**
 * Takes a sequence of signals and turns it into a signal containing a sequence of the original values
 */
class TransposeSignal[A](signals: Seq[Signal[A]]) extends {
  override val dependencies = signals.toSet[Reactive[_, _, _]]
} with DependentSignalImpl[Seq[A]] with MultiDependentReactive {
  override def reevaluateValue(transaction: Transaction) = signals.map(_.value(transaction))
}
