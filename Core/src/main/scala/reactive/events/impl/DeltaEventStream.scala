package reactive
package events
package impl

import reactive.signals.Signal
import reactive.impl.SingleDependentReactive
import scala.concurrent.stm._

class DeltaEventStream[A](val dependency: Signal[A]) extends DependentEventStreamImpl[(A, A)] with SingleDependentReactive {
  private val lastValue = Ref(dependency.now)
  protected def reevaluate(transaction: Transaction): Option[(A, A)] = {
    atomic { tx =>
      val newValue = dependency.pulse(transaction).asOption.get
      Some(lastValue.swap(newValue)(tx) -> newValue)
    }
  }
}
