package reactive
package signals
package impl

import reactive.events.EventStream
import util.TicketAccumulator
import reactive.impl.SingleDependentReactive

class FoldSignal[A, B](private val initialValue: A, private val source: EventStream[B], op: (A, B) => A) extends DependentSignalImpl[A] with SingleDependentReactive[A] {
  override val dependency = source
  protected def reevaluate(transaction: Transaction): A = {
    if (transaction == null) {
      initialValue
    } else {
      val pulse = source.pulse(transaction)
      if (pulse.isDefined) {
        op(now, pulse.get)
      } else {
        now
      }
    }
  }
}
