package reactive
package signals
package impl

import reactive.impl.MultiDependentReactive

import scala.concurrent.stm.InTxn

class FunctionalSignal[A](private val op: InTxn => A, override val dependencies: Set[Reactive.Dependency], tx: InTxn) extends MultiDependentReactive(tx) with DependentSignalImpl[A] {
  override def reevaluateValue(tx: InTxn) = op(tx)
}
