package reactive
import scala.collection.mutable
import java.util.UUID

class FunctionalSignal[A](name: String, op: => A, dependencies: Signal[_]*) extends SignalImpl[A](name, op) with ReactiveDependant[Any] {
  private val debug = false;

  /**
   * map of for which event how many update notifications are still missing
   * and whether or not any of the dependencies that did already send a
   * notification has actually changed it's value
   */
  private val updateLog = mutable.Map[Event, (Int, Boolean)]()

  dependencies.foreach { _.addDependant(this); }

  override def sourceDependencies = dependencies.foldLeft(Map[UUID, UUID]()) { (accu, dep) => accu ++ dep.sourceDependencies }

  /**
   * this variable reflects the number of total entries in {@link #updateLog}
   * and {@link #suspendedCalculations} with the Boolean value of the value
   * tuple set to <code>true</code>. This is used to defer dirty notifications
   * until the signal actually knows that it needs to recalculate the value
   * rather than marking itself dirty as soon as there's a possible change
   * which might not occur. But, even then, the recalculation might not
   * result in an actual change of the signal's value and the dirty state
   * might just be cleared again without notifying observers of a new value.
   */
  private var numUpdatesWithChangedDependency = 0;

//  private lazy val _dirty = updateLog.synchronized { Var(name + ".dirty", numUpdatesWithChangedDependency > 0) }
//  override def dirty: Signal[Boolean] = _dirty

  override def notifyUpdate(event: Event, value: Any) {
    notifyUpdate(event, true);
  }
  override def notifyEvent(event: Event) {
    notifyUpdate(event, false);
  }

  private def notifyUpdate(event: Event, notifierValueChanged: Boolean) {
    val action = getEmitAction(event, notifierValueChanged);
    if (action.isDefined) {
      updateValue(event, if (action.get) Signal.during(event) { op } else value);
    }
  }

  /**
   * None => don't emit yet
   * Some(false) => emit, but no need to recalculate value
   * Some(true) => recalculate and then emit value
   */
  private def getEmitAction(event: Event, notifierValueChanged: Boolean): Option[Boolean] = {
    updateLog.synchronized {
      val (previouslyPendingNotifications, previouslyAnyDependencyChanged) = updateLog.get(event).getOrElse((dependencies.count { _.isConnectedTo(event) }, false));
      val pendingNotifications = previouslyPendingNotifications - 1;
      val anyDependencyChanged = previouslyAnyDependencyChanged || notifierValueChanged;
      if (pendingNotifications == 0) {
        updateLog -= event;
        Some(anyDependencyChanged)
      } else {
        updateLog += (event -> ((pendingNotifications, anyDependencyChanged)))
        None
      }
    }
  }
}