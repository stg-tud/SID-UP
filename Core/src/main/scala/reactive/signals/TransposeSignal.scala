package reactive.signals

import reactive.Reactive
import reactive.impl.DynamicDependentReactive
import reactive.signals.impl.DependentSignalImpl
import scala.concurrent.stm.InTxn
import scala.collection.TraversableLike
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

/**
 * Takes a sequence of signals and turns it into a signal containing a sequence of the original values
 */
class TransposeSignal[A, C[B] <: TraversableLike[B, C[B]]](signals: Signal[C[Signal[A]]], tx: InTxn)(implicit canBuildFrom: CanBuildFrom[C[_], A, C[A]]) extends DynamicDependentReactive(tx) with DependentSignalImpl[C[A]] {
  override def reevaluateValue(tx: InTxn) = signals.transactional.now(tx).map(_.transactional.now(tx))

  override protected def dependencies(tx: InTxn): Set[Reactive[_, _]] = signals.transactional.now(tx).toSet + signals
}
