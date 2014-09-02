package unoptimized
package signals
package impl

import unoptimized.impl.DynamicDependentReactive

class FlattenSignal[A](val outer: Signal[Signal[A]]) extends DependentSignalImpl[A] with DynamicDependentReactive {
  override def dependencies(transaction: Transaction) = Set(outer, outer.value(transaction))
  override def reevaluateValue(transaction: Transaction) = outer.value(transaction).value(transaction)
}