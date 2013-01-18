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
      if (!(event.sourcesAndPredecessors.keySet & events.sourceDependencies.keySet).isEmpty) {
        if (lock.synchronized {
          if (ignoreForSignal.remove(event)) {
            false
          } else {
            val publish = waitingForSignal.remove(event);
            if (!publish) {
              waitingForEventStream += (event -> newValue);
            }
            publish
          }
        }) {
          updateValue(event, newValue);
        }
      }
    }
  }
  signal.addDependant(signalObserver);
  private val eventsObserver = new ReactiveDependant[Any] {
    def notifyEvent(event: Event) {
      if (!(event.sourcesAndPredecessors.keySet & signal.sourceDependencies.keySet).isEmpty) {
        lock.synchronized {
          if (waitingForEventStream.remove(event).isEmpty) {
            ignoreForSignal += event;
          }
        }
      }
      notifyDependants(event);
    }
    def notifyUpdate(event: Event, newValue: Any) {
      if ((event.sourcesAndPredecessors.keySet & signal.sourceDependencies.keySet).isEmpty) {
        updateValue(event, signal.value);
      } else {
        lock.synchronized {
          waitingForEventStream.remove(event) match {
            case Some(value) => updateValue(event, value);
            case None => waitingForSignal += event
          }
        }
      }
    }
  }
  events.addDependant(eventsObserver);
}