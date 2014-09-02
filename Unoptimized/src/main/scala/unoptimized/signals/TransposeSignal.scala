package unoptimized.signals

import unoptimized.impl.DynamicDependentReactive
import unoptimized.{Reactive, Transaction}
import unoptimized.signals.impl.DependentSignalImpl

/**
 * Takes a sequence of signals and turns it into a signal containing a sequence of the original values
 */
class TransposeSignal[A](signals: Signal[Iterable[Signal[A]]]) extends DependentSignalImpl[Iterable[A]] with DynamicDependentReactive {
  override def reevaluateValue(transaction: Transaction) = signals.value(transaction).map(_.value(transaction))

  override protected def dependencies(transaction: Transaction): Set[Reactive[_, _]] = signals.value(transaction).toSet + signals
}

