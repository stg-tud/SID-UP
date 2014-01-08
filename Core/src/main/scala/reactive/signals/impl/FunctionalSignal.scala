package reactive
package signals
package impl

import reactive.impl.MultiDependentReactive

class FunctionalSignal[A](private val op: Transaction => A, private val inputs: Signal[_]*) extends {
  override val dependencies = inputs.toSet[DependableReactive]
} with DependentSignalImpl[A] with MultiDependentReactive[A, Reactive.IDENTITY, Reactive.IDENTITY, Reactive.IDENTITY, Signal] {
  override def reevaluateValue(transaction: Transaction) = op(transaction)
}
