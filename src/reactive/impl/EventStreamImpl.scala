package reactive.impl

import scala.collection.mutable
import reactive.EventStream
import reactive.Event
import scala.actors.threadpool.TimeoutException

abstract class EventStreamImpl[A](name: String) extends ReactiveImpl[A](name) with EventStream[A] {

  private val valHistory = new mutable.WeakHashMap[Event, Option[A]]();

  protected[this] def maybeNotifyObservers(event: Event, value: Option[A]) {
    valHistory.synchronized {
      valHistory += (event -> value)
      valHistory.notifyAll();
    }
    value.foreach { notifyObservers(event, _); }
  }

  @throws(classOf[TimeoutException])
  override def await(event: Event, timeout : Long = 0): Option[A] = {
    if (!isConnectedTo(event)) {
      throw new IllegalArgumentException("illegal wait: " + event + " will not update this reactive.");
    }
    valHistory.synchronized {
      var value = valHistory.get(event);
      val end = System.currentTimeMillis() + timeout;
      while (value.isEmpty) {
        if (timeout > 0 && end < System.currentTimeMillis()) throw new TimeoutException(name + " timed out waiting for " + event);
        valHistory.wait(timeout);
        value = valHistory.get(event);
      }
      value
    }.get
  }
}