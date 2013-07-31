package reactive
package testtools

import scala.collection.mutable
import java.util.UUID

case class Notification[P](transaction: Transaction, newSourceDependencies: Set[UUID], sourceDependenciesChanged: Boolean, newValue: P, valueChanged: Boolean)

class NotificationLog[P](private val reactive: Reactive[_, P, P]) extends mutable.Queue[Notification[P]] with Reactive.Dependant {
  reactive.addDependant(null, this);
  override def apply(transaction: Transaction, sourceDependenciesChanged : Boolean, pulsed: Boolean) {
    this += new Notification(transaction, reactive.sourceDependencies(transaction), sourceDependenciesChanged, reactive.value(transaction), pulsed)
  }
}
