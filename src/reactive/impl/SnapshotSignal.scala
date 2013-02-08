package reactive.impl

import scala.collection.mutable
import reactive.Signal
import reactive.EventStream
import reactive.Event
import reactive.EventStreamDependant
import reactive.SignalDependant
import reactive.PropagationData

// TODO should not use signal.now, should implement dependency caching equivalent to FunctionalSignal instead
class SnapshotSignal[A](signal: Signal[A], events: EventStream[_]) extends StatelessSignal[A]("snapshot(" + signal.name + ")on(" + events.name + ")", signal.now) with SignalDependant[A] with EventStreamDependant[Any] {
  signal.addDependant(this);
  events.addDependant(this);

  override def sourceDependencies = events.sourceDependencies

  private val lock = new Object();
  private val waitingForEventStream = mutable.Map[Event, A]()
  private val waitingForSignal = mutable.Map[Event, PropagationData]()
  private val ignoreForSignal = mutable.Set[Event]()

  override def notifyEvent(propagationData: PropagationData, value: A, changed: Boolean) {
    val event = propagationData.event
    if (events.isConnectedTo(event)) {
      val maybePropagationData = lock.synchronized {
        if (ignoreForSignal.remove(event)) {
          None
        } else {
          waitingForSignal.remove(event) match {
            case None =>
              waitingForEventStream += (event -> value);
              None
            case x => x
          }
        }
      }
      maybePropagationData.foreach {
        propagate(_, Some(value));
      }
    }
  }
  def notifyEvent(propagationData: PropagationData, maybeValue: Option[Any]) {
    val event = propagationData.event;
    maybeValue match {
      case None =>
        propagate(propagationData, None);
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
              case Some(value) => propagate(propagationData, Some(value));
              case None => waitingForSignal += (event -> propagationData)
            }
          }
        } else {
          propagate(propagationData, Some(signal.now));
        }
    }
  }
}