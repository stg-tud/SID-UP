package unoptimized
package testtools

import scala.collection.mutable
import java.util.UUID
import unoptimized.signals.Signal

case class Notification[P](transaction: Transaction, newSourceDependencies: Set[UUID], sourceDependenciesChanged: Boolean, newValue: P, valueChanged: Boolean)

class NotificationLog[P](private val unoptimized: Signal[P]) extends mutable.Queue[Notification[P]] with Reactive.Dependant {
  unoptimized.addDependant(null, this)

  override def ping(transaction: Transaction): Unit = {
    this += new Notification(transaction, unoptimized.sourceDependencies(transaction), unoptimized.sourceDependenciesChanged(transaction), unoptimized.value(transaction), unoptimized.pulse(transaction).isDefined)
  }
}
