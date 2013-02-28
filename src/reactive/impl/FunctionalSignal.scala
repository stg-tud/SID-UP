package reactive.impl
import scala.collection.mutable
import java.util.UUID
import reactive.Signal
import reactive.Transaction
import reactive.EventStreamDependant
import reactive.Reactive
import reactive.SignalDependant

class FunctionalSignal[A](name: String, op: Signal.ReactiveEvaluationContext => A, dependencies: Signal[_]*) extends {
  private val lastEventsLock = new Object
  private var lastEvents = dependencies.foldLeft(Map[Signal[_], Transaction]()) { (map, dependency) => map + (dependency -> dependency.lastEvent) }
} with SignalImpl[A](name, op(new Signal.ReactiveEvaluationContext(null, lastEvents))) with SignalDependant[Any] {
  private val debug = false;

  private var ordering = new EventOrderingCache[Boolean](sourceDependencies) {
    override def eventReadyInOrder(event: Transaction, anyDependencyChanged: Boolean) {
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
          val newValue = op(new Signal.ReactiveEvaluationContext(event, cachedLastEvents))
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

  override def notifyEvent(event: Transaction, value : Any, changed : Boolean) {
    updateLog.synchronized {
      updateLog.get(event) match {
        case Some(logEntry) =>
          logEntry.receivedNotification(changed);
          if (logEntry.isReady()) {
            updateLog -= event;
            Some(logEntry.anyDependencyChanged)
          } else {
            None
          }
        case None =>
          val expectedNotifications = dependencies.count { _.isConnectedTo(event) }
          if (expectedNotifications == 1) {
            Some(changed)
          } else {
            val newEntry = new UpdateLogEntry(expectedNotifications - 1, changed)
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
  private val updateLog = mutable.Map[Transaction, UpdateLogEntry]()

  override def sourceDependencies = dependencies.foldLeft(Map[UUID, UUID]()) { (accu, dep) => accu ++ dep.sourceDependencies }
}
