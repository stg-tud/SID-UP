package reactive

import scala.collection.mutable

class SnapshotSignal[A](signal: Signal[A], events: EventStream[_]) extends SignalImpl[A]("snapshot(" + signal.name + ")on(" + events.name + ")", signal.value) {

  override def sourceDependencies = events.sourceDependencies

  private val lock = new Object();
  private val waitingForEventStream = mutable.Map[Event, Option[A]]()
  private val waitingForSignal = mutable.Set[Event]()
  private val ignoreForSignal = mutable.Set[Event]()

  private def propagateUpdate(event: Event, maybeNewValue: Option[A]) {
    maybeNewValue match {
      case Some(newValue) => this.maybeNewValue(event, newValue);
      case None => noNewValue(event);
    }
  }
  private val signalObserver = new ReactiveDependant[A] {
    def notifyEvent(event: Event) {
      notifyUpdate(event, None)
    }
    def notifyUpdate(event: Event, newValue: A) {
      notifyUpdate(event, Some(newValue))
    }
    private def notifyUpdate(event: Event, maybeNewValue: Option[A]) {
      if (events.isConnectedTo(event)) {
        val shouldEmit = lock.synchronized {
          if (ignoreForSignal.remove(event)) {
            false
          } else {
            val shouldEmit = waitingForSignal.remove(event);
            if (!shouldEmit) {
              waitingForEventStream += (event -> maybeNewValue);
            }
            shouldEmit
          }
        }
        if (shouldEmit) {
          propagateUpdate(event, maybeNewValue);
        }
      }

    }
  }
  signal.addDependant(signalObserver);
  private val eventsObserver = new ReactiveDependant[Any] {
    def notifyEvent(event: Event) {
      noNewValue(event);
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
            case Some(maybeNewValue) => propagateUpdate(event, maybeNewValue);
            case None => waitingForSignal += event
          }
        }
      } else {
        maybeNewValue(event, signal.value);
      }
    }
  }
  events.addDependant(eventsObserver);
}