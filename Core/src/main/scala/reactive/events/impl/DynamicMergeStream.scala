package reactive.events.impl

import reactive.impl.DynamicDependentReactive
import reactive.events.EventStream
import reactive.{Transaction, Reactive}

class DynamicMergeStream[A]() extends {
  private var streams = Seq[EventStream[A]]()
} with DependentEventStreamImpl[A] with DynamicDependentReactive {

  def addEvents(events: EventStream[A]*): Unit = {
    events.foreach { _.addDependant(null, this) }
    streams ++= events
  }

  override protected def dependencies(transaction: Transaction): Set[Reactive[_, _]] = streams.toSet

  protected def reevaluate(transaction: Transaction): Option[A] = {
    streams.find {
      _.pulse(transaction).isDefined
    }.flatMap {
      _.pulse(transaction)
    }
  }
}
