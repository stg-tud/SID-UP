package reactive
package events
package impl

import reactive.impl.SingleDependentReactive

import scala.concurrent.stm.InTxn

class TransformEventStream[A, B](val dependency: EventStream[B], private val op: Option[B] => Option[A], tx: InTxn) extends SingleDependentReactive(tx) with DependentEventStreamImpl[A] {
  protected def reevaluate(tx: InTxn): Option[A] = op(dependency.pulse(tx).asOption)
}
