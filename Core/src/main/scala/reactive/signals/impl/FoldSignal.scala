package reactive
package signals
package impl

import reactive.events.EventStream
import reactive.impl.SingleDependentReactive

import scala.concurrent.stm._

class FoldSignal[A, B](private val initialValue: A, val dependency: EventStream[B], op: (A, B) => A, tx: InTxn) extends {
  override protected val value = Ref(initialValue)
} with SingleDependentReactive(tx) with DependentSignalImpl[A] {
  protected override def reevaluateValue(tx: InTxn) = {
    dependency.pulse(tx) match {
      case Reactive.Changed(v) => op(now(tx), v)
      case _ => now(tx)
    }
  }
}
