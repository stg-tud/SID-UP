package reactive
package events
package impl

import reactive.impl.SingleDependentReactive

import scala.concurrent.stm.InTxn

class FilteredEventStream[A](val dependency: EventStream[A], private val op: A => Boolean, tx: InTxn) extends SingleDependentReactive(tx) with DependentEventStreamImpl[A] {
  protected def reevaluate(tx: InTxn): Option[A] = dependency.pulse(tx).asOption.filter(op)
}
