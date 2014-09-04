package unoptimized
package signals
package impl

import unoptimized.impl.MultiDependentReactive

class FunctionalSignal[A](private val op: Transaction => A, private val inputs: Signal[_]*) extends {
  override val dependencies = inputs.toSet[Reactive[_, _]]
} with DependentSignalImpl[A] with MultiDependentReactive {
  override def reevaluateValue(transaction: Transaction) = op(transaction)
}
