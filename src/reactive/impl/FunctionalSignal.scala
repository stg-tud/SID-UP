package reactive.impl
import scala.collection.mutable
import java.util.UUID
import reactive.Signal
import reactive.Event
import reactive.ReactiveDependant

class FunctionalSignal[A](name: String, op: => A, dependencies: Signal[_]*) extends {
  private var currentValues = DependencyValueCache.initializeInstance(dependencies: _*);
} with SignalImpl[A](name, Signal.withContext(currentValues) { op }) {
  private val debug = false;

  private var ordering: EventOrderingCache[(Option[A], DependencyValueCache)] = new EventOrderingCache[(Option[A], DependencyValueCache)](sourceDependencies) {
    override def eventReadyInOrder(event: Event, data: (Option[A], DependencyValueCache)) {
      val (maybeNewValue, recalculate) = data
      updateValue(event) { currentValue =>
        currentValues = recalculate;
        maybeNewValue.getOrElse(currentValue);
      };
    }
  }

  class UpdateLogEntry(var pendingUpdates: Int) {
    val dependencyValues = new DependencyValueCache(currentValues)
    var anyDependencyChanged = false;
    def receivedNotification[A](dependency: Signal[A], maybeValue: Option[A]) {
      pendingUpdates -= 1;
      maybeValue.foreach { value =>
        dependencyValues.set(dependency, value);
        anyDependencyChanged = true;
      }
    }
  }

  private def createUpdateLogEntry[A](event: Event) = new UpdateLogEntry(dependencies.count { _.isConnectedTo(event) });

  dependencies.foreach { connect(_) }
  private def connect[A](dependency: Signal[A]) {
    dependency.addDependant(new ReactiveDependant[A] {
      /**
       * None => don't emit yet
       * Some(false) => emit, but no need to recalculate value
       * Some(true) => recalculate and then emit value
       */
      private def getEmitAction(event: Event, maybeValue: Option[A]): Option[UpdateLogEntry] = {
        updateLog.synchronized {
          val logEntry = updateLog.get(event).getOrElse {
            val newEntry = createUpdateLogEntry(event);
            if (newEntry.pendingUpdates > 1) updateLog += (event -> newEntry);
            newEntry
          }
          logEntry.receivedNotification(dependency, maybeValue);

          if (logEntry.pendingUpdates == 0) {
            updateLog -= event;
            Some(logEntry)
          } else {
            updateLog += (event -> logEntry)
            None
          }
        }
      }

      override def notifyEvent(event: Event, value: Option[A]) {
        getEmitAction(event, value).foreach { recalculate =>
          if (recalculate.anyDependencyChanged) {
            val newValue = Signal.withContext(recalculate.dependencyValues) { op };
            ordering.eventReady(event, (Some(newValue), recalculate.dependencyValues));
          } else {
            ordering.eventReady(event, (None, recalculate.dependencyValues));
          }
        }
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
