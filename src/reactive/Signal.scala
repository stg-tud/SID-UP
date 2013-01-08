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
   * map of happened-before-event.uuid to suspended updates for which all
   * expected notifications have been received, but one or more happened-before
   * relations have not completed yet
   */
  private val suspendedCalculations = mutable.Map[UUID, Tuple3[mutable.Set[UUID], Event, Boolean]]()
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

  @volatile private var _dirty: Var[Boolean] = null;
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
          val requiredPredecessors = mutable.Set[UUID]()
          event.sourcesAndPredecessors.foreach { case (source, predecessor) =>
            if(predecessor != null && incomingEdgesPerSource.contains(source)) {
              lastEvents.get(source) match {
                case Some(lastEvent) if(predecessor.equals(lastEvent)) => // predecessor requirement already fulfilled
                case _ => requiredPredecessors += predecessor // record predecessor requirement
              }
            } 
          }
          if (requiredPredecessors.isEmpty) {
            // event has no predecessor or event is next in line: process
            Some((event, valueChanged))
          } else {
            // event has predecessor other than last processed event: defer to prevent out-of-order update
            val suspendedCalculation = (requiredPredecessors, event, valueChanged);
            requiredPredecessors.foreach { predecessor =>
            	suspendedCalculations += (predecessor -> suspendedCalculation)
            }
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
          val expectedEdges = event.sourcesAndPredecessors.keys.foldLeft(Set[Reactive[_]]()) { (accu, source) => accu ++ incomingEdgesPerSource.get(source).get }
          getUpdateData(expectedEdges.size - 1, notifierValueChanged)
      }
    }
    while (updateData.isDefined) {
      val (event: Event, valueChanged: Boolean) = updateData.get
      updateLog.synchronized {
        if (valueChanged) numUpdatesWithChangedDependency -= 1;
        if (_dirty != null) _dirty.set(numUpdatesWithChangedDependency > 0)
        event.sourcesAndPredecessors.keys.foreach { source =>
          lastEvents += (source -> event.uuid)
        }
      }
      // somewhat important: the actual evaluation and notification of
      // observers and dependencies happens outside of synchronization.
      // This especially means that it is possible for observers
      // to be notified of a new value while the value actually has
      // changed again already.
      updateValue(event, if (valueChanged) Reactive.during(event) { op } else value);
      updateData = updateLog.synchronized {
        suspendedCalculations.get(event.uuid) match {
          case Some((missingPredecessors, event, valueChanged)) =>
            missingPredecessors -= event.uuid
            if(missingPredecessors.isEmpty) {
              suspendedCalculations -= event.uuid
              Some(event, valueChanged)
            } else {
              None
            }
          case None => None
        }
      }
    }
  }
}

object Signal {
  def apply[A](name: String, signals: Reactive[_]*)(op: => A) = new Signal[A](name, op, signals: _*);
  def apply[A](signals: Reactive[_]*)(op: => A): Signal[A] = apply("AnonSignal", signals: _*)(op)
}