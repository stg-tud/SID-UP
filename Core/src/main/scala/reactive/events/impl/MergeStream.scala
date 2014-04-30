package reactive
package events
package impl

import reactive.impl.MultiDependentReactive

class MergeStream[A](private val streams: Iterable[EventStream[A]]) extends {
  override val dependencies = streams.toSet : Set[Reactive.Dependency]
} with DependentEventStreamImpl[A] with MultiDependentReactive {

  protected def reevaluate(transaction: Transaction): Option[A] = {
    streams.find {
      _.pulse(transaction).changed
    }.flatMap {
      _.pulse(transaction).asOption
    }
  }
}