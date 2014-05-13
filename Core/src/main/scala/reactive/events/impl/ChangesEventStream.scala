package reactive
package events
package impl

import reactive.signals.Signal
import reactive.impl.SingleDependentReactive
import scala.concurrent.stm.InTxn

class ChangesEventStream[A](val dependency: Signal[A], tx: InTxn) extends SingleDependentReactive(tx) with DependentEventStreamImpl[A] {
  protected def reevaluate(tx: InTxn): Option[A] = dependency.pulse(tx).asOption
}
