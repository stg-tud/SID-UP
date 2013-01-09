package reactive

import scala.collection.Iterable
import java.util.UUID

abstract class DependantReactive[A](name : String, initialValue : A, dependencies: Reactive[_]*) extends Reactive[A](name, initialValue, dependencies.foldLeft(Set[Event]()) {(set, dependency) => set ++ dependency.knownEvents}) {
  dependencies.foreach { _.addDependant(this) }
//  override val level = dependencies.foldLeft(0)((max, signal) => math.max(max, signal.level + 1))

  protected[reactive] def notifyUpdate(event: Event, valueChanged: Boolean);
}