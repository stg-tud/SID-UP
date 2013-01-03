package asdf
import scala.collection.mutable.MutableList
import util.ThreadPool

class Signal[A](name: String, op: => A, signals: Reactive[_]*) extends Reactive[A](name, op) {
  val level = signals.foldLeft(0)((max, signal) => math.max(max, signal.level + 1))
  private val incomingEdgesPerSource = signals.foldLeft(Map[Var[_], Int]()) { (map: Map[Var[_], Int], signal) =>
    signal.sourceDependencies.foldLeft(map) { (map: Map[Var[_], Int], source) =>
      map + (source -> (map.get(source) match {
        case Some(x) => x + 1
        case None => 1
      }))
    }
  }

  val sourceDependencies = incomingEdgesPerSource.keys

  signals.foreach { _.addDependant(this) }

  @volatile private var pendingUpdates = 0;
  @volatile private var anyDependencyChanged = false;

  private val lock = new Object();

  def notifyUpdate(pool : ThreadPool, source: Var[_], valueChanged: Boolean) {
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