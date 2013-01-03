package asdf
import scala.collection.mutable.PriorityQueue

class Propagator {
  implicit object QueueOrdering extends Ordering[Reactive[_]] {
    override def compare(a: Reactive[_], b: Reactive[_]) = {
      b.level - a.level;
    }
  }

  def run(source: Reactive[_]) {
    val pending = new PriorityQueue[Reactive[_]]();
    pending += source;
    while (!pending.isEmpty) {
      val elem = pending.dequeue();
      if (elem.updateValue()) {
        pending ++= elem.dependencies
      }
    }
  }
}