package reactive.impl
import scala.collection.mutable
import java.util.UUID
import reactive.Signal
import reactive.Event
import reactive.ReactiveDependant

class FunctionalSignal[A](name: String, op: => A, dependencies: Signal[_]*) extends {
  private val lastEventsLock = new Object
  private var lastEvents = dependencies.foldLeft(Map[Signal[_], Event]()) { (map, dependency) => map + (dependency -> dependency.lastEvent) }
} with SignalImpl[A](name, Signal.withContext(null, lastEvents) { op }) {
  private val debug = false;

  private var ordering = new EventOrderingCache[UpdateLogEntry](sourceDependencies) {
    override def eventReadyInOrder(event: Event, data: UpdateLogEntry) {
      lastEventsLock.synchronized {
        dependencies.foreach { dependency =>
          if (dependency.isConnectedTo(event)) {
            lastEvents += (dependency -> event)
          }
        }
      }

      if (data.anyDependencyChanged) {
        val newValue = Signal.withContext(event, lastEvents) { op }
        updateValue(event) { _ => newValue };
      } else {
        updateValue(event) { oldValue => oldValue }
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

  dependencies.foreach { connect(_) }
  private def connect[A](dependency: Signal[A]) {
    dependency.addDependant(new ReactiveDependant[A] {
      override def notifyEvent(event: Event, maybeValue: Option[A]) {
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
    })
  }

  /**
   * map of for which event how many update notifications are still missing
   * and whether or not any of the dependencies that did already send a
   * notification has actually changed it's value
   */
  private val updateLog = mutable.Map[Event, UpdateLogEntry]()

  override def sourceDependencies = dependencies.foldLeft(Map[UUID, UUID]()) { (accu, dep) => accu ++ dep.sourceDependencies }
}
