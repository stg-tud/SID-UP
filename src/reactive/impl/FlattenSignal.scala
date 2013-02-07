package reactive.impl

import reactive.Signal
import reactive.ReactiveDependant
import reactive.Event
import scala.collection.mutable

// TODOs:
// - make thread safe
// - make order preserving
// - add source dependencies update
// - wait for inner notification only if inner has not yet processed event (need to make this information accessible first)
class FlattenSignal[A, B](name: String, outer: Signal[B], op: B => Signal[A]) extends {
  var currentInner = op(outer.now);
} with StatelessSignal[A](name, currentInner.now) {
  override def sourceDependencies = Map()

  class LogEntry(val event: Event) {
    val affectsOuter = outer.isConnectedTo(event)
    var outerNotification: Option[B] = null
    var affectsInner = currentInner.isConnectedTo(event)
    var innerNotification: Option[A] = null
    def receiveOuterNotification(maybeValue: Option[B]) {
      outerNotification = maybeValue;
      if (outerNotification.isDefined) {
        val newInner = op(outerNotification.get)
        if (newInner.equals(currentInner)) {
          if(!affectsInner) {
            innerNotification = None
          }
        } else {
	      currentInner.removeDependant(innerObserver)
	      currentInner = newInner
	      currentInner.addDependant(innerObserver)
          affectsInner = newInner.isConnectedTo(event);
          innerNotification = if(affectsInner) null else Some(newInner.now) 
        }
      }
    }
    def receiveInnerNotification(maybeValue: Option[A]) {
      innerNotification = maybeValue;
    }

    def isOuterReady = !affectsOuter || outerNotification != null
    def isInnerReady = !affectsInner || innerNotification != null
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

  val outerObserver = new ReactiveDependant[B] {
    override def notifyEvent(event: Event, maybeValue: Option[B]) {
      val logEntry = getLogEntry(event);
      logEntry.receiveOuterNotification(maybeValue);
      updated(logEntry);
    }
  }
  outer.addDependant(outerObserver);
  val innerObserver = new ReactiveDependant[A] {
    override def notifyEvent(event: Event, maybeValue: Option[A]) {
      val logEntry = getLogEntry(event);
      logEntry.receiveInnerNotification(maybeValue);
      updated(logEntry);
    }
  }
  currentInner.addDependant(innerObserver)

  def updated(logEntry: LogEntry) {
    if (logEntry.isReady) {
      logEntries -= logEntry.event;
      propagate(logEntry.event, logEntry.innerNotification)
    }
  }
}