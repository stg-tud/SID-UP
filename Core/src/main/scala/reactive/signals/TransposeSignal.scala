package reactive.signals

import reactive.impl.DynamicDependentReactive
import reactive.{ Reactive, Transaction }
import reactive.signals.impl.DependentSignalImpl
import scala.concurrent.stm.InTxn

/**
 * Takes a sequence of signals and turns it into a signal containing a sequence of the original values
 */
class TransposeSignal[A](signals: Signal[Iterable[Signal[A]]], tx: InTxn) extends DynamicDependentReactive(tx) with DependentSignalImpl[Iterable[A]] {
  override def reevaluateValue(tx: InTxn) = signals.now(tx).map(_.now(tx))

  override protected def dependencies(tx: InTxn): Set[Reactive[_, _]] = signals.now(tx).toSet + signals
}
