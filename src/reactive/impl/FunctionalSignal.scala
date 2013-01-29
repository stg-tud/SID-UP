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

  private var ordering = new EventOrderingCache[Boolean](sourceDependencies) {
    override def eventReadyInOrder(event: Event, anyDependencyChanged: Boolean) {
      val cachedLastEvents = lastEventsLock.synchronized {
        dependencies.foreach { dependency =>
          if (dependency.isConnectedTo(event)) {
            lastEvents += (dependency -> event)
          }
        }
        lastEvents
      }

      Reactive.executePooled {
        if (anyDependencyChanged) {
          val newValue = Signal.withContext(event, cachedLastEvents) { op }
          propagate(event, Some(newValue));
        } else {
          propagate(event, None);
        }
      }
    }
  }

  private class UpdateLogEntry(var pendingUpdates: Int, var anyDependencyChanged: Boolean) {
    def receivedNotification(dependencyChanged: Boolean) {
      anyDependencyChanged |= dependencyChanged
      pendingUpdates -= 1;
    }
    def isReady() = pendingUpdates == 0
  }

  dependencies.foreach { _.addDependant(this) }

  override def notifyEvent(event: Event, maybeValue: Option[Any]) {
    val dependencyChanged = maybeValue.isDefined;
    updateLog.synchronized {
      updateLog.get(event) match {
        case Some(logEntry) =>
          logEntry.receivedNotification(dependencyChanged);
          if (logEntry.isReady()) {
            updateLog -= event;
            Some(logEntry.anyDependencyChanged)
          } else {
            None
          }
        case None =>
          val expectedNotifications = dependencies.count { _.isConnectedTo(event) }
          if (expectedNotifications == 1) {
            Some(dependencyChanged)
          } else {
            val newEntry = new UpdateLogEntry(expectedNotifications - 1, dependencyChanged)
            updateLog += (event -> newEntry);
            None
          }
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
