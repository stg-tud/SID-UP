package reactive
package impl
package mirroring

import reactive.signals.impl.SignalImpl

class MirroredSignal[A](initialValue: A, initialSourceDependencies: Reactive.Topology) extends MirroredReactive[A](initialSourceDependencies) with SignalImpl[A] with ReactiveNotificationDependant[A] {
  private var value = initialValue
  def now = value
  def value(t: Transaction) = value
  override def pulseReceived(pulse: A) {
    value = pulse
  }
}