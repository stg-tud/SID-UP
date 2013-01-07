package reactive
import scala.collection.mutable
import java.util.UUID

class Signal[A](name: String, op: => A, dependencies: Reactive[_]*) extends DependantReactive[A](name, op, dependencies: _*) {
  /**
   * for which event are how many update notifications still missing and
   * has any of the dependencies that did notify already changed the value?
   */
  private var updateLog = mutable.Map[Event, Tuple2[Int, Boolean]]()
  /**
   * this variable reflects the number of total entries in {@link #updateLog}
   * with the Boolean value of the value tuple set to <code>true</code>
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

  protected[reactive] override def notifyUpdate(event: Event, valueChanged: Boolean) {
    updateLog.synchronized {
      def update(pendingUpdates: Int, valueChanged: Boolean) = {
        if (pendingUpdates == 0) {
          updateLog -= event;
          if (valueChanged) numUpdatesWithChangedDependency -= 1;
          if (_dirty != null) _dirty.set(numUpdatesWithChangedDependency > 0)
          Some((event, valueChanged))
        } else {
          updateLog += (event -> (pendingUpdates, valueChanged))
          if (_dirty != null) _dirty.set(true)
          None
        }
      }
 
      updateLog.get(event) match {
        case Some((pendingUpdates, anyDependencyChangedSoFar)) =>
          if (!anyDependencyChangedSoFar && valueChanged) numUpdatesWithChangedDependency += 1;
          update(pendingUpdates - 1, anyDependencyChangedSoFar || valueChanged)
        case None =>
          if (valueChanged) numUpdatesWithChangedDependency += 1;
          update(incomingEdgesPerSource.get(event.source).get - 1, valueChanged)
      }
    } match {
      case Some((event, valueChanged)) =>
        updateValue(event, if (valueChanged) op else value);
      case None =>
    }

  }
}

object Signal {
  def apply[A](name: String, signals: Reactive[_]*)(op: => A) = new Signal[A](name, op, signals: _*);
  def apply[A](signals: Reactive[_]*)(op: => A): Signal[A] = apply("AnonSignal", signals: _*)(op)
}