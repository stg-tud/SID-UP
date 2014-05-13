package reactive
package events
package impl

import reactive.impl.MultiDependentReactive
import scala.concurrent.stm.InTxn

class MergeStream[A](private val streams: Iterable[EventStream[A]], tx: InTxn) extends {
  override val dependencies = streams.toSet : Set[Reactive.Dependency]
} with MultiDependentReactive(tx) with DependentEventStreamImpl[A] {

  protected def reevaluate(tx: InTxn): Option[A] = {
    streams.find {
      _.pulse(tx).changed
    }.flatMap {
      _.pulse(tx).asOption
    }
  }
}