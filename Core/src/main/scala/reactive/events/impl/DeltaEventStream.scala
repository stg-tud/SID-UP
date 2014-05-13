package reactive
package events
package impl

import reactive.signals.Signal
import reactive.impl.SingleDependentReactive
import scala.concurrent.stm._

class DeltaEventStream[A](val dependency: Signal[A], tx: InTxn) extends SingleDependentReactive(tx) with DependentEventStreamImpl[(A, A)] {
  private val lastValue = Ref(dependency.single.now)
  protected def reevaluate(tx: InTxn): Option[(A, A)] = {
      val newValue = dependency.pulse(tx).asOption.get
      Some(lastValue.swap(newValue)(tx) -> newValue)
  }
}
