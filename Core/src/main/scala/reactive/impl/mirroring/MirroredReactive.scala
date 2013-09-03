package reactive
package impl
package mirroring

abstract class MirroredReactive[P](initialSourceDependencies: Reactive.Topology) extends ReactiveNotificationDependant[P] {
  self: ReactiveImpl[_, _, P, _] =>
  private var _sourceDependencies = initialSourceDependencies
  override def sourceDependencies(transaction: Transaction) = _sourceDependencies
  
  override def fire(notification: ReactiveNotification[P]) {
    notification.sourceDependencies.foreach { _sourceDependencies = _ }
    notification.pulse.foreach{pulseReceived(_)}
    doPulse(notification.transaction, notification.sourceDependencies.isDefined, notification.pulse)
  }
  
  def pulseReceived(pulse: P)
}