package reactive
import scala.collection.mutable
import java.util.UUID

class Signal[A](name: String, op: => A, dependencies: Reactive[_]*) extends DependantReactive[A](name, op, dependencies: _*) {
  private val debug = false;

  /**
   * map of for which event how many update notifications are still missing
   * and whether or not any of the dependencies that did already send a
   * notification has actually changed it's value
   */
  private val updateLog = mutable.Map[Event, (Int, Boolean)]()
  /**
   * map of source.uuid to event.uuid of last event that was completed. Only
   * the direct predecessor event of that last event can be propagated next,
   * all other completed events are used to {@link #suspendedCalculations}
   */
  private val lastEvents = mutable.Map[UUID, UUID]()
  private val incomingEdgesPerSource = mutable.Map[UUID, Set[Reactive[_]]]();

  dependencies.foreach { dependency =>
    dependency.sourceDependencies.foreach {
      case (source, event) =>
        lastEvents.get(source) match {
          case Some(x) => if (!x.equals(event)) throw new IllegalStateException("Cannot create new signal while events are in transit!");
          case None => lastEvents += (source -> event)
        }

        incomingEdgesPerSource += (source -> (incomingEdgesPerSource.get(source) match {
          case Some(x) => x + dependency
          case None => Set(dependency)
        }));
    }
  }

  override def sourceDependencies = lastEvents.toMap
  /**
   * map of happened-before-event.uuid to suspended updates for which all
   * expected notifications have been received, but one or more happened-before
   * relations have not completed yet
   */
  private val suspendedCalculations = mutable.Map[UUID, List[(mutable.Set[UUID], Event, Boolean)]]()
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
          event.sourcesAndPredecessors.foreach {
            case (source, predecessor) =>
              if (lastEvents.contains(source)) {
                lastEvents.get(source) match {
                  case Some(lastEvent) if (predecessor.equals(lastEvent)) =>
                  // predecessor requirement already fulfilled, so don't record it
                  case _ =>
                    // predecessor not available or different than required => record requirement
                    requiredPredecessors += predecessor
                }
              }
          }
          if (requiredPredecessors.isEmpty) {
            // event has no predecessor or event is next in line: process
            Some((event, valueChanged))
          } else {
            if (debug) println("suspending evaluation for event " + event + " due to missing predecessors " + requiredPredecessors)
            // event has predecessor other than last processed event: defer to prevent out-of-order update
            val suspendedCalculation = (requiredPredecessors, event, valueChanged);
            requiredPredecessors.foreach { predecessor =>
              suspendedCalculations += (predecessor -> (suspendedCalculations.get(predecessor) match {
                case Some(x) => suspendedCalculation :: x
                case _ => List(suspendedCalculation)
              }))
            }
            if (debug) println("suspended calculations now: " + suspendedCalculations);
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
      updateData = None;
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
      if (debug) print("evaluating " + event + ": ")
      updateValue(event, if (valueChanged) {
        val newValue = Reactive.during(event) { op };
        if (debug) println("calculated new value " + newValue);
        newValue;
      } else {
        if (debug) println("no dependency changed, not recalculating value");
        value
      });
      updateLog.synchronized {
        suspendedCalculations.remove(event.uuid) match {
          case Some(list) =>
            list.foreach {
              case (missingPredecessors, followUpEvent, valueChanged) =>
                if (debug) print("updating suspended calculation of " + followUpEvent + " waiting for " + missingPredecessors + " -> ");
                missingPredecessors.remove(event.uuid)
                if (missingPredecessors.isEmpty) {
                  if (debug) println("no predecessors left, scheduling immediate execution.");
                  updateData match {
                    case Some(x) => throw new AssertionError("processing one event triggered processing of multiple successors, this should not be possible.");
                    case None => updateData = Some(followUpEvent, valueChanged)
                  }
                } else {
                  if (debug) println("now waiting for " + missingPredecessors);
                }
            }
            if (debug) println("suspended calculations now: " + suspendedCalculations);
          case None =>
            if (debug) println("no suspended calculations depending on " + event.uuid);
        }
      }
    }
  }
}

object Signal {
  def apply[A](name: String, signals: Reactive[_]*)(op: => A) = new Signal[A](name, op, signals: _*);
  def apply[A](signals: Reactive[_]*)(op: => A): Signal[A] = apply("AnonSignal", signals: _*)(op)
}