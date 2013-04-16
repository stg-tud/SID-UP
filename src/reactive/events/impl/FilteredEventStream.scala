package reactive
package events
package impl

class FilteredEventStream[A](from: EventStream[A], op: A => Boolean) extends EventStreamImpl[A](from.sourceDependencies) with EventStream.Dependant[A] {
  from.addDependant(None, this)
  override def notify(notification : EventNotification[A]) {
    publish(new EventNotification(notification.transaction, notification.sourceDependenciesUpdate.applyTo(_sourceDependencies), notification.maybeValue.filter(op)));
  }
}
