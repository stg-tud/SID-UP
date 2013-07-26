package reactive
package events
package impl

import reactive.impl.MultiDependentReactive

class MergeStream[A](private val streams: Iterable[EventStream[A]]) extends {
  override val dependencies = streams.toSet : Set[Reactive[_, _, _]]
} with EventStreamImpl[A] with MultiDependentReactive[A] {

  protected def calculatePulse(transaction: Transaction): Option[A] = {
    streams.find {
      _.pulse(transaction).isDefined
    }.flatMap {
      _.pulse(transaction)
    }
  }
}