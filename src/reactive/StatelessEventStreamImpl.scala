package reactive

import scala.collection.mutable

abstract class StatelessEventStreamImpl[A](name: String) extends EventStreamImpl[A](name) with EventStream[A] {
  /**
   * lazily instantiated when first observer is added
   */
  private var ordering: EventOrderingCache[Option[A]] = null
  override def observe(obs: A => Unit) {
    if (ordering == null) ordering = new EventOrderingCache[Option[A]](sourceDependencies) {
      override def eventReadyInOrder(event: Event, data: Option[A]) {
        maybeNotifyObservers(event, data);
      }
    }
    super.observe(obs);
  }

  def propagate(event: Event, maybeValue: Option[A]) {
    notifyDependants(event, maybeValue);
    if (ordering != null) ordering.eventReady(event, maybeValue);
  }
}