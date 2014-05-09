package reactive
package events
package impl

import reactive.signals.Signal
import reactive.impl.SingleDependentReactive
import scala.concurrent.stm.InTxn

class ChangesEventStream[A](val dependency: Signal[A]) extends DependentEventStreamImpl[A] with SingleDependentReactive {
  protected def reevaluate(tx: InTxn): Option[A] = dependency.pulse(tx).asOption
}
