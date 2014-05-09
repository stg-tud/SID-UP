package reactive
package events
package impl

import reactive.impl.SingleDependentReactive
import scala.concurrent.stm.InTxn

class FilteredEventStream[A](val dependency: EventStream[A], private val op: A => Boolean) extends DependentEventStreamImpl[A] with SingleDependentReactive {
  protected def reevaluate(tx: InTxn): Option[A] = dependency.pulse(tx).asOption.filter(op)
}
