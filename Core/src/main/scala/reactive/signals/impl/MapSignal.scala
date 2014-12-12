package reactive
package signals
package impl

import reactive.impl.SingleDependentReactive

import scala.concurrent.stm.InTxn

class MapSignal[A, B](override val dependency: Signal[B], op: B => A, tx: InTxn) extends SingleDependentReactive(tx) with DependentSignalImpl[A] {
  protected def reevaluateValue(tx: InTxn): A = op(dependency.transactional.now(tx))
}
