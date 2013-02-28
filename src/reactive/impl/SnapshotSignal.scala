package reactive.impl

import scala.collection.mutable
import reactive.Signal
import reactive.EventStream
import reactive.Transaction
import reactive.EventStreamDependant
import reactive.SignalDependant

// TODO should not use signal.now, should implement dependency caching equivalent to FunctionalSignal instead
class SnapshotSignal[A](signal: Signal[A], events: EventStream[_]) extends Signal[A]("snapshot(" + signal.name + ")on(" + events.name + ")", signal.now) with SignalDependant[A] with EventStreamDependant[Any] {
  signal.addDependant(this);
  events.addDependant(this);

  override def sourceDependencies = events.sourceDependencies

  private val lock = new Object();
  private val waitingForEventStream = mutable.Map[Transaction, A]()
  private val waitingForSignal = mutable.Set[Transaction]()
  private val ignoreForSignal = mutable.Set[Transaction]()

  override def notifyEvent(event: Transaction, value: A, changed: Boolean) {
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
  def notifyEvent(event: Transaction, maybeValue: Option[Any]) {
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