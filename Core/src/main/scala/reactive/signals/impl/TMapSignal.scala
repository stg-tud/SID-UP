package reactive
package signals
package impl

import reactive.impl.SingleDependentReactive
import scala.concurrent.stm.InTxn

class TMapSignal[A, B](override val dependency: Signal[B], op: (B, InTxn) => A, tx: InTxn) extends SingleDependentReactive(tx) with DependentSignalImpl[A] {
  protected def reevaluateValue(tx: InTxn): A = op(dependency.now(tx), tx)
}
