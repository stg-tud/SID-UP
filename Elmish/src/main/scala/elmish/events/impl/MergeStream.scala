package elmish
package events
package impl

import elmish.impl.MultiDependentReactive

class MergeStream[A](private val streams: Iterable[EventStream[A]]) extends {
  override val dependencies = streams.toSet : Set[Reactive[_, _, _]]
} with DependentEventStreamImpl[A] with MultiDependentReactive {

  protected def reevaluatePulse(transaction: Transaction): Option[A] = {
    streams.find {
      _.pulse(transaction).isDefined
    }.flatMap {
      _.pulse(transaction)
    }
  }
}