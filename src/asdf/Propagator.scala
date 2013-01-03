package asdf
import scala.collection.mutable

class Propagator {
  implicit object QueueOrdering extends Ordering[Reactive[_]] {
    override def compare(a: Reactive[_], b: Reactive[_]) = {
      b.level - a.level;
    }
  }

  def run(source: Var[_]) {
    // TODO: shortcut unnecessary traversal of trees below [ready += (x, false)] nodes
    val ready = new mutable.PriorityQueue[Tuple2[Reactive[_], Boolean]]();
    val waiting = mutable.Map[Signal[_], Tuple2[Int, Boolean]]();
    ready += Tuple2[Reactive[_], Boolean](source, true);
    while (!ready.isEmpty) {
      val (elem, dependenciesChanged) = ready.dequeue();
      val currentChanged = dependenciesChanged && elem.updateValue();
      elem.dependencies.foreach { dep =>
        waiting.get(dep) match {
          case Some((pendingNotifications, otherDependenciesChanged)) =>
            val nowChanged = currentChanged || otherDependenciesChanged;
            if (pendingNotifications == 1) {
              waiting -= dep;
              ready += Tuple2[Reactive[_], Boolean](dep, nowChanged);
            } else {
              waiting += (dep -> (pendingNotifications - 1, nowChanged))
            }
          case None =>
            val count = dep.incomingEdgesPerSource.get(source).get;
            if (count == 1) {
              ready += Tuple2[Reactive[_], Boolean](dep, currentChanged);
            } else {
              waiting += (dep -> (count - 1, currentChanged))
            }
        }
      }
    }
    if (!waiting.isEmpty) throw new RuntimeException("Something went wrong. Missing update notifications for signals: " + waiting)
  }
}