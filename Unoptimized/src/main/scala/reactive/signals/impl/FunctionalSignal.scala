package reactive
package signals
package impl

import reactive.impl.MultiDependentReactive

class FunctionalSignal[A](private val op: Transaction => A, private val inputs: Signal[_]*) extends {
  override val dependencies = inputs.toSet[Reactive.Dependency]
} with DependentSignalImpl[A] with MultiDependentReactive {
  override def reevaluateValue(transaction: Transaction) = op(transaction)
}
