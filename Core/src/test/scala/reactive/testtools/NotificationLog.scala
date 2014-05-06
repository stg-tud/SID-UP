package reactive
package testtools

import scala.collection.mutable
import java.util.UUID
import reactive.signals.Signal
import reactive.impl.{ReactiveImpl, SingleDependentReactive, DependentReactive}
import reactive.signals.impl.{DependentSignalImpl, SignalImpl}
import reactive.events.impl.{EventStreamImpl, DependentEventStreamImpl}

case class Notification[P](transaction: Transaction, newSourceDependencies: Set[UUID], sourceDependenciesChanged: Boolean, newValue: P, valueChanged: Boolean)

class NotificationLog[P](override val dependency: Signal[P]) extends  DependentEventStreamImpl[P] with SingleDependentReactive {
  //TODO: this should NOT be in a comment
  //reactive.addDependant(null, this)

  val q = mutable.Queue[Notification[P]]()

  def size = q.size
  def dequeue() = q.dequeue()
  def isEmpty = q.isEmpty

  override def ping(transaction: Transaction) = {
    q += new Notification(transaction,
      dependency.sourceDependencies(transaction),
      sourceDependenciesChanged = dependency.pulse(transaction).sourceDependencies.isDefined,
      dependency.value(transaction),
      valueChanged = dependency.pulse(transaction).value.isDefined)
    super.ping(transaction)
  }

  // Members declared in reactive.impl.DependentReactive
  override protected def reevaluate(transaction: reactive.Transaction): Option[P] = dependency.pulse(transaction).value


}
