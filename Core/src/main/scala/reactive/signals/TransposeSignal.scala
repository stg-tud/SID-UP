package reactive.signals

import reactive.impl.DynamicDependentReactive
import reactive.{ Reactive, Transaction }
import reactive.signals.impl.DependentSignalImpl
import scala.language.higherKinds
import scala.collection.generic.CanBuildFrom

/**
 * Takes a sequence of signals and turns it into a signal containing a sequence of the original values
 */
class TransposeSignal[A, C[B] <: Traversable[B]](signals: Signal[C[Signal[A]]])(implicit canBuildFrom: CanBuildFrom[C[_], A, C[A]]) extends DependentSignalImpl[C[A]] with DynamicDependentReactive {
  override def reevaluateValue(transaction: Transaction) = {
    val list = signals.value(transaction)
    val builder = canBuildFrom.apply(list);
    builder.sizeHint(list.size)
    for(signal <- list) builder += signal.value(transaction)
    builder.result
  }

  override protected def dependencies(transaction: Transaction): Set[Reactive[_, _]] = signals.value(transaction).toSet + signals
}
