package reactive
package testtools

import scala.collection.mutable
import java.util.UUID
import reactive.signals.Signal

case class Notification[P](transaction: Transaction, newSourceDependencies: Set[UUID], sourceDependenciesChanged: Boolean, newValue: P, valueChanged: Boolean)

class NotificationLog[P](private val reactive: Signal[P]) extends mutable.Queue[Notification[P]] with Reactive.Dependant {
  //TODO: this should NOT be in a comment
  //reactive.addDependant(null, this)

  override def ping(transaction: Transaction) {
    this += new Notification(transaction, reactive.sourceDependencies(transaction), sourceDependenciesChanged = false, reactive.value(transaction), valueChanged = true)
  }
}
