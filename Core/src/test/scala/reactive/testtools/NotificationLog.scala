package reactive
package testtools

import scala.collection.mutable
import java.util.UUID

case class Notification[V, P](transaction: Transaction, newSourceDependencies: Set[UUID], sourceDependenciesChanged: Boolean, value: V, pulse: Option[P])

class NotificationLog[V, P](private val reactive: Reactive[_, V, P, _]) extends mutable.Queue[Notification[V, P]] with Reactive.Dependant {
  reactive.addDependant(null, this);
  override def apply(transaction: Transaction, sourceDependenciesChanged : Boolean, pulsed: Boolean) {
    this += new Notification(transaction, reactive.sourceDependencies(transaction), sourceDependenciesChanged, reactive.value(transaction), reactive.pulse(transaction))
  }
}
