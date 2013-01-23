package reactive

import scala.collection.mutable

abstract class EventStreamImpl[A](name: String) extends ReactiveImpl[A](name) with EventStream[A] {

  /**
   * lazily instantiated when first observer is added
   */
  private var ordering: EventOrderingCache[Option[A]] = null
  override def observe(obs: A => Unit) {
    if (ordering == null) ordering = new EventOrderingCache[Option[A]](sourceDependencies) {
      override def eventReadyInOrder(event: Event, data: Option[A]) {
        valHistory.synchronized {
	      valHistory += (event -> data)
	      valHistory.notifyAll();
        }
        data.foreach { notifyObservers(event, _); }
      }
    }
    super.observe(obs);
  }

  private val valHistory = new mutable.WeakHashMap[Event, Option[A]]();

  override def awaitMaybeEvent(event: Event): Option[A] = {
    if (!isConnectedTo(event)) {
      throw new IllegalArgumentException("illegal wait: " + event + " will not update this reactive.");
    }
    valHistory.synchronized {
      var value = valHistory.get(event);
      while (value.isEmpty) {
        valHistory.wait();
        value = valHistory.get(event);
      }
      value
    }.get
  }

  override protected def notifyDependants(event: Event) {
    super.notifyDependants(event);
    if (ordering != null) ordering.eventReady(event, None);
  }

  override protected def notifyDependants(event: Event, newValue: A) {
    super.notifyDependants(event, newValue);
    if (ordering != null) ordering.eventReady(event, Some(newValue));
  }
}