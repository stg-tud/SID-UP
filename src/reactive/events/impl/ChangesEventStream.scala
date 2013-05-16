package reactive
package events
package impl

import reactive.signals.Signal
import reactive.signals.SignalNotification

class ChangesEventStream[A](from: Signal[A]) extends EventStreamImpl[A](from.sourceDependencies) with Signal.Dependant[A] {
  from.addDependant(None, this)
  override def notify(notification: SignalNotification[A]) {
    publish(new EventNotification(notification.transaction, notification.sourceDependenciesUpdate.applyTo(_sourceDependencies), notification.valueUpdate.newValueIfChanged))
  }
}