package reactive
package signals
package impl

import reactive.impl.MultiDependentReactive
import scala.concurrent.stm.InTxn

class FunctionalSignal[A](private val op: InTxn => A, private val inputs: Iterable[Signal[_]], tx: InTxn) extends {
  override val dependencies = inputs.toSet[Reactive.Dependency]
} with MultiDependentReactive(tx) with DependentSignalImpl[A] {
  override def reevaluateValue(tx: InTxn) = op(tx)
}
