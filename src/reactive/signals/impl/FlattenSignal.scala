package reactive
package signals
package impl

import util.MutableValue
import util.TransactionalTransientVariable
import util.TicketAccumulator
import util.COMMIT

class FlattenSignal[A](val outer: Signal[Signal[A]]) extends {
  // TODO handle outer exception here
  //  var inner: Option[Signal[A]] = Some(outer.now)
  var inner: Signal[A] = outer.now
  //} with SignalImpl[A](outer.sourceDependencies ++ inner.get.sourceDependencies, Left(inner.get.now)) {
} with SignalImpl[A](outer.sourceDependencies ++ inner.sourceDependencies, inner.now) {
  private val outerNotification = new TransactionalTransientVariable[(TicketAccumulator.Receiver, SignalNotification[Signal[A]])]

  outer.addDependant(None, new Signal.Dependant[Signal[A]] {
    override def notify(replyChannel: TicketAccumulator.Receiver, notification: SignalNotification[Signal[A]]) {
      if (notification.valueUpdate.changed) {
        inner.removeDependant(innerDependant)
        inner = notification.valueUpdate.newValue
        inner.addDependant(Some(notification.transaction), innerDependant) match {
          case Some(innerNotification) =>
            publish(new SignalNotification(
              notification.transaction,
              _sourceDependencies.update(notification.sourceDependenciesUpdate.newValue ++ innerNotification.sourceDependenciesUpdate.newValue),
              value.update(innerNotification.valueUpdate.newValue)), replyChannel);
          case None =>
            if (!inner.isConnectedTo(notification.transaction)) {
              publish(new SignalNotification(notification.transaction,
                _sourceDependencies.update(inner.sourceDependencies ++ notification.sourceDependenciesUpdate.newValue),
                value.update(inner()(notification.transaction))), replyChannel)
            } else {
              outerNotification.set(notification.transaction, (replyChannel, notification));
            }
        }
      } else {
        outerNotification.set(notification.transaction, (replyChannel, notification));
      }
    }
  })
  val innerDependant = new Signal.Dependant[A] {
    override def notify(replyChannel: TicketAccumulator.Receiver, notification: SignalNotification[A]) {
      if (outer.isConnectedTo(notification.transaction)) {
        replyChannel(COMMIT)
        outerNotification.getIfSet(notification.transaction).foreach {
          case (outerReplyChannel, outerNotification) =>
            publish(new SignalNotification(
              notification.transaction,
              _sourceDependencies.update(outerNotification.sourceDependenciesUpdate.newValue ++ notification.sourceDependenciesUpdate.newValue),
              value.update(notification.valueUpdate.newValue)), outerReplyChannel);
        }
      } else {
        publish(new SignalNotification(
          notification.transaction,
          notification.sourceDependenciesUpdate.applyToMapped(_sourceDependencies, { _ ++ outer.sourceDependencies }),
          notification.valueUpdate.applyTo(value)), replyChannel)
      }
    }
  }
  inner.addDependant(None, innerDependant)
}