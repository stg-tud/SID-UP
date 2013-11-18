package reactive.signals

import reactive.impl.MultiDependentReactive
import reactive.{Reactive, Transaction}
import reactive.signals.impl.DependentSignalImpl

/**
 * Takes a sequence of signals and turns it into a signal containing a sequence of the original values
 */
class TransposeSignal[A](signals: Seq[Signal[A]]) extends {
  override val dependencies = signals.toSet[Reactive[_, _, _]]
} with DependentSignalImpl[Seq[A]] with MultiDependentReactive {
  override def reevaluateValue(transaction: Transaction) = signals.map(_.value(transaction))
}
