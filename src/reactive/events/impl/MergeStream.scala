package reactive
package events
package impl

import java.util.UUID


class MergeStream[A](streams: Iterable[EventStream[A]]) extends EventStreamImpl[A](streams.foldLeft(Set[UUID]()) { (set, dep) => set ++ dep.sourceDependencies }) with EventStream.Dependant[A] {
  streams.foreach{ _.addDependant(this) }
  private var pending = 0;
  private var anyDependencyChange : Boolean = _
  private var event : Option[A] = _

  override def notify(notification: EventNotification[A]) {
    if (pending == 0) {
      pending = streams.count(_.isConnectedTo(notification.transaction))
      anyDependencyChange = false
      event = None
    }
    
    anyDependencyChange |= notification.sourceDependenciesUpdate.changed
    if(notification.maybeValue.isDefined) event = notification.maybeValue
    pending -= 1;
    
    if (pending == 0) {
      val sourceDependencyUpdate = if (anyDependencyChange) {
        _sourceDependencies.update(streams.foldLeft(Set[UUID]()) { (set, dep) => set ++ dep.sourceDependencies })
      } else {
        _sourceDependencies.noChangeUpdate
      }

      publish(new EventNotification(notification.transaction, sourceDependencyUpdate, event))
    }
  }
}