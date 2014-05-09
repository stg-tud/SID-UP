package reactive
package signals
package impl

import reactive.impl.MultiDependentReactive
import scala.concurrent.stm.InTxn

class FunctionalSignal[A](private val op: InTxn => A, private val inputs: Signal[_]*) extends {
  override val dependencies = inputs.toSet[Reactive.Dependency]
} with DependentSignalImpl[A] with MultiDependentReactive {
  override def reevaluateValue(tx: InTxn) = op(tx)
}
