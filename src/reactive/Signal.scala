package reactive
import scala.collection.mutable.MutableList
import util.ThreadPool
import java.util.UUID

class Signal[A](name: String, op: => A, dependencies: Reactive[_]*) extends Reactive[A](name, op) {
  dependencies.foreach { _.addDependant(this) }
  val level = dependencies.foldLeft(0)((max, signal) => math.max(max, signal.level + 1))
  private val incomingEdgesPerSource = dependencies.foldLeft(Map[UUID, Int]()) { (map, signal) =>
    signal.sourceDependencies.foldLeft(map) { (map, source) =>
      map + (source -> (map.get(source) match {
        case Some(x) => x + 1
        case None => 1
      }))
    }
  }
  val sourceDependencies = incomingEdgesPerSource.keys


  @volatile private var pendingUpdates = 0;
  @volatile private var anyDependencyChanged = false;

  private val lock = new Object();

  def notifyUpdate(pool : ThreadPool, source: UUID, valueChanged: Boolean) {
    if (lock.synchronized {
      anyDependencyChanged |= valueChanged
      if (pendingUpdates == 0) {
        pendingUpdates = incomingEdgesPerSource.get(source).get - 1
      } else {
        pendingUpdates -= 1;
      }
      pendingUpdates == 0
    }) {
      // assuming there are not multiple source events running in parallel,
      // this block will only be executed by the last notification and ca
      // thus read and write the volatile fields without a semaphore
      if (anyDependencyChanged) {
        anyDependencyChanged = false;
        updateValue(pool, source, op);
      } else {
        notifyDependencies(pool, source, false);
      }
    }
  }
}

object Signal {
  def apply[A](name: String, signals: Reactive[_]*)(op: => A) = new Signal[A](name, op, signals: _*);
  def apply[A](signals: Reactive[_]*)(op: => A): Signal[A] = apply("AnonSignal", signals: _*)(op)
}