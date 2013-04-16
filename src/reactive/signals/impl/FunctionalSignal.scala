package reactive
package signals
package impl

import scala.collection.mutable
import java.util.UUID
import util.Util

class FunctionalSignal[A](op: Transaction => A, dependencies: Signal[_]*) extends SignalImpl[A](dependencies.foldLeft(Set[UUID]()) { (set, dep) => set ++ dep.sourceDependencies }, op(null)) with Signal.Dependant[Any] {
  dependencies.foreach { _.addDependant(this) }
  private var pending = 0;
  private var anyDependencyChange = false
  private var anyValueChange = false
  override def notify(notification: SignalNotification[Any]) {
    if (pending == 0) {
      pending = dependencies.count(_.isConnectedTo(notification.transaction))
      anyDependencyChange = false
      anyValueChange = false
    }

    anyDependencyChange |= notification.sourceDependenciesUpdate.changed
    anyValueChange |= notification.valueUpdate.changed
    pending -= 1;

    if (pending == 0) {
      val sourceDependencyUpdate = if (anyDependencyChange) {
        _sourceDependencies.update(dependencies.foldLeft(Set[UUID]()) { (set, dep) => set ++ dep.sourceDependencies })
      } else {
        _sourceDependencies.noChangeUpdate
      }

      val valueUpdate = if (anyValueChange) {
        value.update(op(notification.transaction))
      } else {
        value.noChangeUpdate
      }

      publish(new SignalNotification(notification.transaction, sourceDependencyUpdate, valueUpdate))
    }
  }
}
