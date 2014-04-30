package reactive
package signals
package impl

import reactive.events.EventStream
import reactive.impl.SingleDependentReactive

//TODO: this whole signal looks very suspicious, the use of initial value when there is no transaction, as well as now is just begging to cause horrible errors
class FoldSignal[A, B](private val initialValue: A, val dependency: EventStream[B], op: (A, B) => A) extends DependentSignalImpl[A] with SingleDependentReactive {
  protected override def reevaluateValue(transaction: Transaction) = {
    if (transaction == null) {
      initialValue
    } else {
      dependency.pulse(transaction) match {
        case Reactive.Changed(v) => op(now, v)
        case _ => now
      }
    }
  }
}
