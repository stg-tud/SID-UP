package reactive
package signals
package impl

import reactive.events.EventStream
import reactive.impl.SingleDependentReactive
import scala.concurrent.stm._

class FoldSignal[A, B](private val initialValue: A, val dependency: EventStream[B], op: (A, B) => A) extends DependentSignalImpl[A] with SingleDependentReactive {
  override protected val value = Ref(initialValue)
  protected override def reevaluateValue(tx: InTxn) = {
    dependency.pulse(tx) match {
      case Reactive.Changed(v) => op(now(tx), v)
      case _ => now(tx)
    }
  }
}
