package reactive
package impl
package mirroring

import reactive.events.impl.EventStreamImpl


class MirroredEventStream[A](initialSourceDependencies: Reactive.Topology) extends MirroredReactive[A](initialSourceDependencies) with EventStreamImpl[A] {
  def now = ()
  def value(t: Transaction) = ()
  override def pulseReceived(pulse: A) {}
}