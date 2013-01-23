package reactive

import scala.collection.mutable

class SnapshotSignal[A](signal: Signal[A], events: EventStream[_]) extends StatelessSignal[A]("snapshot(" + signal.name + ")on(" + events.name + ")", signal.value) {

  override def sourceDependencies = events.sourceDependencies

  private val lock = new Object();
  private val waitingForEventStream = mutable.Map[Event, Option[A]]()
  private val waitingForSignal = mutable.Set[Event]()
  private val ignoreForSignal = mutable.Set[Event]()

  private val signalObserver = new ReactiveDependant[A] {
    override def notifyEvent(event: Event, maybeValue: Option[A]) {
      if (events.isConnectedTo(event)) {
        val shouldEmit = lock.synchronized {
          if (ignoreForSignal.remove(event)) {
            false
          } else {
            val shouldEmit = waitingForSignal.remove(event);
            if (!shouldEmit) {
              waitingForEventStream += (event -> maybeValue);
            }
            shouldEmit
          }
        }
        if (shouldEmit) {
          propagate(event, maybeValue);
        }
      }

    }
  }
  signal.addDependant(signalObserver);
  private val eventsObserver = new ReactiveDependant[Any] {
    def notifyEvent(event: Event, maybeValue: Option[Any]) {
      maybeValue match {
        case None =>
          propagate(event, None);
          if (signal.isConnectedTo(event)) {
            lock.synchronized {
              if (waitingForEventStream.remove(event).isEmpty) {
                ignoreForSignal += event;
              }
            }
          }
        case Some(_) =>
          if (signal.isConnectedTo(event)) {
            lock.synchronized {
              waitingForEventStream.remove(event) match {
                case Some(maybeNewValue) => propagate(event, maybeNewValue);
                case None => waitingForSignal += event
              }
            }
          } else {
            propagate(event, Some(signal.value));
          }
      }
    }
  }
  events.addDependant(eventsObserver);
}