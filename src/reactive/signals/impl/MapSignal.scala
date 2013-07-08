package reactive
package signals
package impl

import reactive.events.EventStream
import reactive.impl.SingleDependentReactive
import util.Util
import util.TicketAccumulator
import util.Update

class MapSignal[A, B](override val dependency: Signal[B], op: B => A) extends DependentSignalImpl[A] with SingleDependentReactive[A] {
  protected def reevaluate(transaction: Transaction): A = op(dependency.value(transaction))
}