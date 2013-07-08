package reactive
package signals
package impl

import reactive.events.EventStream
import reactive.impl.SingleDependentReactive
import util.Util
import util.TicketAccumulator
import util.Update

class MapSignal[A, B](private val from: Signal[B], op: B => A) extends DependentSignalImpl[A] with SingleDependentReactive[A] {
  override val dependency = from
  protected def reevaluate(transaction: Transaction): A = op(from.value(transaction))
}