package reactive
package events
package impl

import reactive.impl.SingleDependentReactive
import reactive.signals.Signal

import scala.concurrent.stm._

class DeltaEventStream[A](val dependency: Signal[A], constructionTransaction: InTxn) extends SingleDependentReactive(constructionTransaction) with DependentEventStreamImpl[(A, A)] {
  private val lastValue = Ref(dependency.single.now)
  protected def reevaluate(tx: InTxn): Option[(A, A)] = {
      val newValue = dependency.pulse(tx).asOption.get
      Some(tx.synchronized(lastValue.swap(newValue)(tx)) -> newValue)
  }
}
