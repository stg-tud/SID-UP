package reactive

import scala.collection.Iterable
import java.util.UUID

abstract class DependantReactive[A](name : String, initialValue : A, dependencies: Reactive[_]*) extends Reactive[A](name, initialValue) {
  dependencies.foreach { _.addDependant(this) }
//  override val level = dependencies.foldLeft(0)((max, signal) => math.max(max, signal.level + 1))
  protected[this] val incomingEdgesPerSource = dependencies.foldLeft(Map[UUID, Int]()) { (map, signal) =>
    signal.sourceDependencies.foldLeft(map) { (map, source) =>
      map + (source -> (map.get(source) match {
        case Some(x) => x + 1
        case None => 1
      }))
    }
  }
  override val sourceDependencies = incomingEdgesPerSource.keys

  protected[reactive] def notifyUpdate(source: UUID, event: UUID, valueChanged: Boolean);
}