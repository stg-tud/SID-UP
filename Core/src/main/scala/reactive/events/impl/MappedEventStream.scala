package reactive
package events
package impl

import reactive.impl.SingleDependentReactive
import scala.concurrent.stm.InTxn

class MappedEventStream[A, B](val dependency: EventStream[B], private val op: B => A, tx: InTxn) extends SingleDependentReactive(tx) with DependentEventStreamImpl[A] {
  protected def reevaluate(tx: InTxn): Option[A] = dependency.pulse(tx).asOption.map(op)
}
