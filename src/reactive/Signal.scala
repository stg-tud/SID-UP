package reactive
import scala.collection.mutable
import java.util.UUID

class Signal[A](name: String, op: => A, dependencies: Reactive[_]*) extends DependantReactive[A](name, op, dependencies: _*) {
  private var updateLog = mutable.Map[UUID, Tuple2[Int, Boolean]]()

  protected[reactive] override def notifyUpdate(source: UUID, event: UUID, valueChanged: Boolean) {
    updateLog.synchronized {
      val eventState = updateLog.get(event) match {
        case Some((pendingUpdates, anyDependencyChangedSoFar)) =>
          (pendingUpdates - 1, anyDependencyChangedSoFar || valueChanged)
        case None =>
          (incomingEdgesPerSource.get(source).get - 1, valueChanged)
      }

      if (eventState._1 == 0) {
        updateLog -= event;
        updateValue(source, event, if (eventState._2) op else value);
      } else {
        updateLog += (event -> eventState)
      }
    }
  }
}

object Signal {
  def apply[A](name: String, signals: Reactive[_]*)(op: => A) = new Signal[A](name, op, signals: _*);
  def apply[A](signals: Reactive[_]*)(op: => A): Signal[A] = apply("AnonSignal", signals: _*)(op)
}