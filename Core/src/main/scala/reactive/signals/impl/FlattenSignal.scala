package reactive
package signals
package impl

import reactive.impl.DynamicDependentReactive
import scala.concurrent.stm.InTxn

class FlattenSignal[A](val outer: Signal[Signal[A]]) extends DependentSignalImpl[A] with DynamicDependentReactive {
  override def dependencies(tx: InTxn) = Set(outer, outer.now(tx))
  override def reevaluateValue(tx: InTxn) = outer.now(tx).now(tx)
}