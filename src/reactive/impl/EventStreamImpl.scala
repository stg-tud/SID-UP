package reactive.impl

import scala.collection.mutable
import reactive.EventStream
import reactive.Event

abstract class EventStreamImpl[A](name: String) extends ReactiveImpl[A](name) with EventStreamDefaults[A] {

  private val valHistory = new mutable.WeakHashMap[Event, Option[A]]();

  protected[this] def maybeNotifyObservers(event: Event, value: Option[A]) {
    valHistory.synchronized {
      valHistory += (event -> value)
      valHistory.notifyAll();
    }
    value.foreach { notifyObservers(event, _); }
  }

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
}