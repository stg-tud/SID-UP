package reactive
package testtools

import scala.collection.mutable
import java.util.UUID
import reactive.signals.Signal

case class Notification[P](transaction: Transaction, newSourceDependencies: Set[UUID], sourceDependenciesChanged: Boolean, newValue: P, valueChanged: Boolean)

class NotificationLog[P](private val reactive: Signal[P]) extends mutable.Queue[Notification[P]] with Reactive.Dependant {
  scala.concurrent.stm.atomic { reactive.addDependant(_, this) }

  override def ping(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = {
    this += new Notification(transaction, reactive.sourceDependencies(transaction.stmTx), sourceDependenciesChanged, reactive.transactional.now(transaction.stmTx), pulsed)
  }
}
