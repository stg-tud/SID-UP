package reactive
package signals
package impl

import util.MutableValue
import util.TransactionalTransientVariable
import util.TicketAccumulator
import util.COMMIT
import reactive.impl.DynamicDependentReactive

class FlattenSignal[A](val outer: Signal[Signal[A]]) extends DependentSignalImpl[A] with DynamicDependentReactive[A] {
  override def dependencies(transaction: Transaction) = Set(outer, outer.value(transaction))
  override def reevaluate(transaction: Transaction) = outer.value(transaction).value(transaction)

  //  // TODO handle outer exception here
  //  //  var inner: Option[Signal[A]] = Some(outer.now)
  //  var inner: Signal[A] = outer.now
  //  //} with SignalImpl[A](outer.sourceDependencies ++ inner.get.sourceDependencies, Left(inner.get.now)) {
  //  private val outerNotification = new TransactionalTransientVariable[(TicketAccumulator.Receiver, Signal.Notification[Signal[A]])]
  //
  //  outer.addDependant(None, new Signal.Dependant[Signal[A]] {
  //    override def notify(replyChannel: TicketAccumulator.Receiver, notification: Signal.Notification[Signal[A]]) {
  //      if (notification.pulse.changed) {
  //        inner.removeDependant(innerDependant)
  //        inner = notification.pulse.newValue
  //        inner.addDependant(Some(notification.transaction), innerDependant) match {
  //          case Some(innerNotification) =>
  //            publish(new Signal.Notification(
  //              notification.transaction,
  //              _sourceDependencies.update(notification.sourceDependenciesUpdate.newValue ++ innerNotification.sourceDependenciesUpdate.newValue),
  //              value.update(innerNotification.pulse.newValue)), replyChannel);
  //          case None =>
  //            if (!inner.isConnectedTo(notification.transaction)) {
  //              publish(new Signal.Notification(notification.transaction,
  //                _sourceDependencies.update(inner.sourceDependencies ++ notification.sourceDependenciesUpdate.newValue),
  //                value.update(inner()(notification.transaction))), replyChannel)
  //            } else {
  //              outerNotification.set(notification.transaction, (replyChannel, notification));
  //            }
  //        }
  //      } else {
  //        outerNotification.set(notification.transaction, (replyChannel, notification));
  //      }
  //    }
  //  })
  //  val innerDependant = new Signal.Dependant[A] {
  //    override def notify(replyChannel: TicketAccumulator.Receiver, notification: Signal.Notification[A]) {
  //      if (outer.isConnectedTo(notification.transaction)) {
  //        replyChannel(COMMIT)
  //        outerNotification.getIfSet(notification.transaction).foreach {
  //          case (outerReplyChannel, outerNotification) =>
  //            publish(new Signal.Notification(
  //              notification.transaction,
  //              _sourceDependencies.update(outerNotification.sourceDependenciesUpdate.newValue ++ notification.sourceDependenciesUpdate.newValue),
  //              value.update(notification.pulse.newValue)), outerReplyChannel);
  //        }
  //      } else {
  //        publish(new Signal.Notification(
  //          notification.transaction,
  //          notification.sourceDependenciesUpdate.applyToMapped(_sourceDependencies, { _ ++ outer.sourceDependencies }),
  //          notification.pulse.applyTo(value)), replyChannel)
  //      }
  //    }
  //  }
  //  inner.addDependant(None, innerDependant)
}