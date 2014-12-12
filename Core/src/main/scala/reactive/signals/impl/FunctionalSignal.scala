package reactive
package signals
package impl

import reactive.impl.MultiDependentReactive

class FunctionalSignal[A](private val op: Transaction => A, override val dependencies: Set[Reactive[_, _]]) extends DependentSignalImpl[A] with MultiDependentReactive {
  override def reevaluateValue(transaction: Transaction) = op(transaction)
}
