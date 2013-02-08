package reactive.impl

import scala.collection.mutable
import reactive.EventStream
import reactive.Event
import java.util.UUID
import reactive.PropagationData

abstract class StatelessEventStreamImpl[A](name: String) extends EventStreamImpl[A](name) with EventStream[A] {
  /**
   * lazily instantiated when first observer is added
   */
  private var ordering: EventOrderingCache[Option[A]] = null
  override def observe(obs: A => Unit) {
    if (ordering == null) ordering = new EventOrderingCache[Option[A]](sourceDependencies) {
      override def eventReadyInOrder(propagationData : PropagationData, data: Option[A]) {
        maybeNotifyObservers(propagationData.event, data);
      }
    }
    super.observe(obs);
  }

  def propagate(propagationData : PropagationData, maybeValue: Option[A]) {
    notifyDependants(propagationData, maybeValue);
    if (ordering != null) ordering.eventReady(propagationData, maybeValue);
  }
}