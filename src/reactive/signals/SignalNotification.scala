package reactive
package signals

import java.util.UUID
import util.Update

case class SignalNotification[+A](transaction : Transaction, sourceDependenciesUpdate : Update[Set[UUID]], valueUpdate: Update[A]) extends ReactiveNotification[A]