package reactive
package events
package impl

import reactive.impl.SingleDependentReactive
import scala.concurrent.stm.InTxn

class PartiallyMappedEventStream[A, B](val dependency: EventStream[B], private val op: PartialFunction[B, A]) extends DependentEventStreamImpl[A] with SingleDependentReactive {
  protected def reevaluate(tx: InTxn): Option[A] = dependency.pulse(tx).asOption.collect(op)
}
