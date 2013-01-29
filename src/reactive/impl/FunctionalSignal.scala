package reactive.impl
import scala.collection.mutable
import java.util.UUID
import reactive.Signal
import reactive.Event
import reactive.ReactiveDependant
import reactive.Reactive

class FunctionalSignal[A](name: String, op: => A, dependencies: Signal[_]*) extends {
  private val lastEventsLock = new Object
  private var lastEvents = dependencies.foldLeft(Map[Signal[_], Event]()) { (map, dependency) => map + (dependency -> dependency.lastEvent) }
} with StatelessSignal[A](name, Signal.withContext(null, lastEvents) { op }) with ReactiveDependant[Any] {
  private val debug = false;

  private var ordering = new EventOrderingCache[UpdateLogEntry](sourceDependencies) {
    override def eventReadyInOrder(event: Event, data: UpdateLogEntry) {
      val cachedLastEvents = lastEventsLock.synchronized {
        dependencies.foreach { dependency =>
          if (dependency.isConnectedTo(event)) {
            lastEvents += (dependency -> event)
          }
        }
        lastEvents
      }

      Reactive.executePooled {
        if (data.anyDependencyChanged) {
          val newValue = Signal.withContext(event, cachedLastEvents) { op }
          propagate(event, Some(newValue));
        } else {
          propagate(event, None);
        }
      }
    }
  }

  private class UpdateLogEntry(var pendingUpdates: Int) {
    var anyDependencyChanged = false;
    def receivedNotification(dependencyChanged: Boolean) {
      pendingUpdates -= 1;
      anyDependencyChanged |= dependencyChanged
    }
  }

  dependencies.foreach { _.addDependant(this) }
  
  override def notifyEvent(event: Event, maybeValue: Option[Any]) {
    updateLog.synchronized {
      val logEntry = updateLog.get(event).getOrElse {
        val newEntry = new UpdateLogEntry(dependencies.count { _.isConnectedTo(event) })
        //            println("expecting "+newEntry.pendingUpdates+" notifications for " + event);
        if (newEntry.pendingUpdates > 1) updateLog += (event -> newEntry);
        newEntry
      }
      logEntry.receivedNotification(maybeValue.isDefined);

      if (logEntry.pendingUpdates == 0) {
        //            println("Event ready: "+event);
        updateLog -= event;
        Some(logEntry)
      } else {
        //            println("Event still missing "+logEntry.pendingUpdates+" notifications: "+event);
        updateLog += (event -> logEntry)
        None
      }
    }.foreach { ordering.eventReady(event, _) }
  }

  /**
   * map of for which event how many update notifications are still missing
   * and whether or not any of the dependencies that did already send a
   * notification has actually changed it's value
   */
  private val updateLog = mutable.Map[Event, UpdateLogEntry]()

  override def sourceDependencies = dependencies.foldLeft(Map[UUID, UUID]()) { (accu, dep) => accu ++ dep.sourceDependencies }
}
