package reactive
package events
package impl

import java.util.UUID
import util.TransactionalAccumulator
import util.TicketAccumulator
import reactive.impl.MultiDependentReactive

class MergeStream[A](private val streams: Iterable[EventStream[A]]) extends EventStreamImpl[A] with MultiDependentReactive[A] {
  override val dependencies = streams.toSet : Set[Reactive[_, _, _]]

  protected def calculatePulse(transaction: Transaction): Option[A] = {
    streams.find {
      _.pulse(transaction).isDefined
    }.flatMap {
      _.pulse(transaction)
    }
  }
}