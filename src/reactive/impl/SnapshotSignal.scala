package reactive.impl

import scala.collection.mutable
import reactive.Signal
import reactive.EventStream
import reactive.Event
import reactive.ReactiveDependant

// TODO should not use signal.now, should implement dependency caching equivalent to FunctionalSignal instead
class SnapshotSignal[A](signal: Signal[A], events: EventStream[_]) extends StatelessSignal[A]("snapshot(" + signal.name + ")on(" + events.name + ")", signal.now) {

  override def sourceDependencies = events.sourceDependencies

  private val lock = new Object();
  private val waitingForEventStream = mutable.Map[Event, A]()
  private val waitingForSignal = mutable.Set[Event]()
  private val ignoreForSignal = mutable.Set[Event]()

  private val signalObserver = new ReactiveDependant[A] {
    override def notifyEvent(event: Event, maybeValue: Option[A]) {
      val value = maybeValue.getOrElse{
       signal.now
      }
      if (events.isConnectedTo(event)) {
        val shouldEmit = lock.synchronized {
          if (ignoreForSignal.remove(event)) {
            false
          } else {
            val shouldEmit = waitingForSignal.remove(event);
            if (!shouldEmit) {
              waitingForEventStream += (event -> value);
            }
            shouldEmit
          }
        }
        if (shouldEmit) {
          propagate(event, Some(value));
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
                case Some(value) => propagate(event, Some(value));
                case None => waitingForSignal += event
              }
            }
          } else {
            propagate(event, Some(signal.now));
          }
      }
    }
  }
  events.addDependant(eventsObserver);
}