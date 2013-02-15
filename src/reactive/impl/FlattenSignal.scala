package reactive.impl

import reactive.Signal
import reactive.EventStreamDependant
import reactive.Event
import scala.collection.mutable
import reactive.SignalDependant

// TODOs:
// - make thread safe
// - make order preserving
// - add source dependencies update
// - block duplicate renotification on inner change
class FlattenSignal[A](outer: Signal[Signal[A]]) extends {
  var currentInner = outer.now;
} with StatelessSignal[A](outer.name + ".flatten", currentInner.now) {
  override def sourceDependencies = Map()

  class LogEntry(val event: Event) {
    val affectsOuter = outer.isConnectedTo(event)
    var receivedOuterNotification = false
    var affectsInner = currentInner.isConnectedTo(event)
    var newValue: Option[A] = null
    def receiveOuterNotification(value: Signal[A], changed: Boolean) {
      receivedOuterNotification = true
      if (changed) {
        currentInner.removeDependant(innerObserver)
        affectsInner = value.isConnectedTo(event);
        currentInner = value
        currentInner.addDependant(innerObserver)
        if (affectsInner) {
          newValue = null;
          currentInner.renotify(innerObserver, event);
        } else {
          newValue = Some(currentInner.now)
        }
      }
    }
    def receiveInnerNotification(maybeValue: Option[A]) {
      newValue = maybeValue;
    }

    def isOuterReady = !affectsOuter || receivedOuterNotification
    def isInnerReady = !affectsInner || newValue != null
    def isReady = isOuterReady && isInnerReady
  }

  val logEntries = mutable.Map[Event, LogEntry]()

  def getLogEntry(event: Event) = {
    logEntries.get(event).getOrElse {
      val entry = new LogEntry(event);
      logEntries += (event -> entry);
      entry
    }
  }

  val outerObserver = new SignalDependant[Signal[A]] {
    override def notifyEvent(event: Event, value: Signal[A], changed: Boolean) {
      logEntries.synchronized {
        val logEntry = getLogEntry(event);
        logEntry.receiveOuterNotification(value, changed);
        updated(logEntry);
      }
    }
  }
  outer.addDependant(outerObserver);
  val innerObserver = new SignalDependant[A] {
    override def notifyEvent(event: Event, value: A, changed: Boolean) {
      logEntries.synchronized {
        val logEntry = getLogEntry(event);
        logEntry.receiveInnerNotification(if (changed) Some(value) else None);
        updated(logEntry);
      }
    }
  }
  currentInner.addDependant(innerObserver)

  def updated(logEntry: LogEntry) {
    if (logEntry.isReady) {
      logEntries -= logEntry.event;
      if (logEntry.newValue == null) throw new AssertionError("That should be impossible");
      propagate(logEntry.event, logEntry.newValue)
    }
  }
}