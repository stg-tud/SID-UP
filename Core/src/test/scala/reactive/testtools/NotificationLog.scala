package reactive
package testtools

import scala.collection.mutable
import java.util.UUID
import reactive.signals.Signal

case class Notification[P](transaction: Transaction, newSourceDependencies: Set[UUID], sourceDependenciesChanged: Boolean, newValue: P, valueChanged: Boolean)

class NotificationLog[P](private val reactive: Signal[P]) extends mutable.Queue[Notification[P]] with Reactive.Dependant {
  reactive.addDependant(null, this)

  override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean) {
    this += new Notification(transaction, reactive.sourceDependencies(transaction.stmTx), sourceDependenciesChanged, reactive.now(transaction.stmTx), pulsed)
  }
}
