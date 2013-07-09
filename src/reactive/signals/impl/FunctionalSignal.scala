package reactive
package signals
package impl

import reactive.impl.MultiDependentReactive

class FunctionalSignal[A](private val op: Transaction => A, private val inputs: Signal[_]*) extends {
  override val dependencies = inputs.toSet[Reactive[_, _, _]]
} with DependentSignalImpl[A] with MultiDependentReactive[A] {
  override def reevaluate(transaction: Transaction) = op(transaction)
}
