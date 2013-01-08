package reactive
import scala.collection.mutable
import java.util.UUID

class Signal[A](name: String, op: => A, dependencies: Reactive[_]*) extends DependantReactive[A](name, op, dependencies: _*) {
  /**
   * map of for which event how many update notifications are still missing
   * and whether or not any of the dependencies that did already send a
   * notification has actually changed it's value
   */
  private val updateLog = mutable.Map[Event, Tuple2[Int, Boolean]]()
  /**
   * map of source.uuid to event.uuid of last event that was completed. Only
   * the direct predecessor event of that last event can be propagated next,
   * all other completed events are used to {@link #suspendedCalculations}
   */
  private val lastEvents = mutable.Map[UUID, UUID]()
  /**
   * map of happened-before-event.uui to suspended updates for which all
   * expected notifications have been received, but the happened-before
   * event has not completed yet
   */
  private val suspendedCalculations = mutable.Map[UUID, Tuple2[Event, Boolean]]()
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

  private var _dirty: Var[Boolean] = null;
  override def dirty: Reactive[Boolean] = {
    if (_dirty == null) {
      updateLog.synchronized {
        if (_dirty == null) {
          _dirty = Var(name + ".dirty", numUpdatesWithChangedDependency > 0)
        }
      }
    }
    return _dirty
  }

  protected[reactive] override def notifyUpdate(event: Event, notifierValueChanged: Boolean) {
    var updateData = updateLog.synchronized {
      def getUpdateData(pendingUpdates: Int, valueChanged: Boolean) = {
        if (pendingUpdates == 0) {
          updateLog -= event;
          val lastEventOfSource = lastEvents.get(event.source)
          if (event.predecessor == null || (lastEventOfSource.isDefined && event.predecessor.equals(lastEventOfSource.get))) {
            // event has no predecessor or event is next in line: process
            Some((event, valueChanged))
          } else {
            // event has predecessor other than last processed event: defer to prevent out-of-order update
            suspendedCalculations += (event.predecessor -> (event, valueChanged))
            None
          }
        } else {
          updateLog += (event -> (pendingUpdates, valueChanged))
          if (_dirty != null) _dirty.set(true)
          None
        }
      }

      updateLog.get(event) match {
        case Some((pendingUpdates, anyDependencyChangedSoFar)) =>
          if (!anyDependencyChangedSoFar && notifierValueChanged) numUpdatesWithChangedDependency += 1;
          getUpdateData(pendingUpdates - 1, anyDependencyChangedSoFar || notifierValueChanged)
        case None =>
          if (notifierValueChanged) numUpdatesWithChangedDependency += 1;
          getUpdateData(incomingEdgesPerSource.get(event.source).get - 1, notifierValueChanged)
      }
    }
    while (updateData.isDefined) {
      val (event: Event, valueChanged: Boolean) = updateData.get
      updateLog.synchronized {
        if (valueChanged) numUpdatesWithChangedDependency -= 1;
        if (_dirty != null) _dirty.set(numUpdatesWithChangedDependency > 0)
        lastEvents += (event.source -> event.uuid)
      }
      updateValue(event, if (valueChanged) Reactive.during(event) { op } else value);
      updateData = updateLog.synchronized {
        suspendedCalculations.remove(event.uuid)
      }
    }
  }
}

object Signal {
  def apply[A](name: String, signals: Reactive[_]*)(op: => A) = new Signal[A](name, op, signals: _*);
  def apply[A](signals: Reactive[_]*)(op: => A): Signal[A] = apply("AnonSignal", signals: _*)(op)
}