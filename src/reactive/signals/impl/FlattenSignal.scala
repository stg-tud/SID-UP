package reactive
package signals
package impl

import util.MutableValue
import util.TransactionalTransientVariable

// TODO
class FlattenSignal[A](val outer: Signal[Signal[A]]) extends {
  var inner = outer.now
} with SignalImpl[A](outer.sourceDependencies ++ inner.sourceDependencies, inner.now) {
  private val outerNotification = new TransactionalTransientVariable[SignalNotification[Signal[A]]]

  outer.addDependant(None, new Signal.Dependant[Signal[A]] {
    override def notify(notification: SignalNotification[Signal[A]]) {
      if(notification.valueUpdate.changed) {
        inner.removeDependant(innerDependant)
      }
      outerNotification.set(notification.transaction, notification);
    }
  })
  val innerDependant = new Signal.Dependant[A] {
    override def notify(notification: SignalNotification[A]) {
      if(outer.isConnectedTo(notification.transaction)) {
        
      }
    }
  }
  inner.addDependant(None, innerDependant)

  def publish() {

  }
}