package reactive

import scala.collection.mutable

class SnapshotSignal[A](signal: Signal[A], events: EventStream[_]) extends SignalImpl[A]("snapshot(" + signal.name + ")on(" + events.name + ")", signal.value) {

  def dirty() = throw new UnsupportedOperationException

  def sourceDependencies() = events.sourceDependencies

  private val lock = new Object();
  private val waitingForEventStream = mutable.Map[Event, A]()
  private val waitingForSignal = mutable.Set[Event]()
  private val ignoreForSignal = mutable.Set[Event]()

  private val signalObserver = new ReactiveDependant[A] {
    def notifyEvent(event: Event) {
      notifyUpdate(event, signal.value)
    }
    def notifyUpdate(event: Event, newValue: A) {
      if (events.isConnectedTo(event)) {
        val shouldEmit = lock.synchronized {
          if (ignoreForSignal.remove(event)) {
            false
          } else {
            val shouldEmit = waitingForSignal.remove(event);
            if (!shouldEmit) {
              waitingForEventStream += (event -> newValue);
            }
            shouldEmit
          }
        }
        if(shouldEmit) {
          updateValue(event, newValue);
        }
      }
    }
  }
  signal.addDependant(signalObserver);
  private val eventsObserver = new ReactiveDependant[Any] {
    def notifyEvent(event: Event) {
      notifyDependants(event);
      if (signal.isConnectedTo(event)) {
        lock.synchronized {
          if (waitingForEventStream.remove(event).isEmpty) {
            ignoreForSignal += event;
          }
        }
      }
    }
    def notifyUpdate(event: Event, newValue: Any) {
      if (signal.isConnectedTo(event)) {
        lock.synchronized {
          waitingForEventStream.remove(event) match {
            case Some(value) => updateValue(event, value);
            case None => waitingForSignal += event
          }
        }
      } else {
        updateValue(event, signal.value);
      }
    }
  }
  events.addDependant(eventsObserver);
}