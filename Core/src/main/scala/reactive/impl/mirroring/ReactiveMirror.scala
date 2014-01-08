package reactive
package impl
package mirroring

import scala.language.higherKinds
import java.util.UUID
trait ReactiveMirror[X, OW[+_], VW[+_], PW[+_], +R[+Y] <: Reactive[Y, OW, VW, PW, R]] {
  def mirror: MirroredReactive[X, OW, VW, PW, R]
}

abstract class MirroredReactive[X, OW[+_], VW[+_], PW[+_], +R[+Y] <: Reactive[Y, OW, VW, PW, R]](private var sourceDependencies: Set[UUID]) extends ReactiveImpl[X, OW, VW, PW, R] with ReactiveNotificationDependant[PW[X]] {
  self: R[X] =>
  override def sourceDependencies(t: Transaction): Set[UUID] = sourceDependencies
  override def fire(notification: ReactiveNotification[PW[X]]) {
    notification.sourceDependencies.foreach { sourceDependencies = _ }
    doPulse(notification.transaction, notification.sourceDependencies.isDefined, notification.pulse)
  }
}