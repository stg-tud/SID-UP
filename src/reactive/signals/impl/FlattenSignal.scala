package reactive
package signals
package impl

import util.MutableValue
import util.TransactionalTransientVariable

class FlattenSignal[A](val outer: Signal[Signal[A]]) extends {
  var inner = outer.now
} with SignalImpl[A](outer.sourceDependencies ++ inner.sourceDependencies, inner.now) {
  private val outerNotification = new TransactionalTransientVariable[SignalNotification[Signal[A]]]

  outer.addDependant(None, new Signal.Dependant[Signal[A]] {
    override def notify(notification: SignalNotification[Signal[A]]) {
      outerNotification.set(notification.transaction, notification);
      if (notification.valueUpdate.changed) {
        inner.removeDependant(innerDependant)
        inner = notification.valueUpdate.newValue
        inner.addDependant(Some(notification.transaction), innerDependant)
        if (!inner.isConnectedTo(notification.transaction)) {
          publish(new SignalNotification(notification.transaction,
            _sourceDependencies.update(inner.sourceDependencies ++ notification.sourceDependenciesUpdate.newValue),
            value.update(inner()(notification.transaction))))
        }
      }
    }
  })
  val innerDependant = new Signal.Dependant[A] {
    override def notify(notification: SignalNotification[A]) {
      if (outer.isConnectedTo(notification.transaction)) {
        outerNotification.getIfSet(notification.transaction).foreach { outerNotification =>
          publish(new SignalNotification(
            notification.transaction,
            _sourceDependencies.update(outerNotification.sourceDependenciesUpdate.newValue ++ notification.sourceDependenciesUpdate.newValue),
            value.update(notification.valueUpdate.newValue)))
        }
      } else {
        publish(new SignalNotification(
          notification.transaction,
          notification.sourceDependenciesUpdate.applyToMapped(_sourceDependencies, { _ ++ outer.sourceDependencies }),
          notification.valueUpdate.applyTo(value)))
      }
    }
  }
  inner.addDependant(None, innerDependant)
}