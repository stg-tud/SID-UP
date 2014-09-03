package unoptimized
package signals
package impl

import unoptimized.events.EventStream
import unoptimized.impl.SingleDependentReactive

class FoldSignal[A, B](private val initialValue: A, val dependency: EventStream[B], op: (A, B) => A) extends DependentSignalImpl[A] with SingleDependentReactive {
  protected override def reevaluateValue(transaction: Transaction) = {
    if (transaction == null) {
      initialValue
    } else {
      val pulse = dependency.pulse(transaction)
      if (pulse.isDefined) {
        op(now, pulse.get)
      } else {
        now
      }
    }
  }
}
