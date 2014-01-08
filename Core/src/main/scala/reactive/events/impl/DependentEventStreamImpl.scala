package reactive
package events
package impl

import reactive.impl.DependentReactive

abstract class DependentEventStreamImpl[A] extends EventStreamImpl[A] with DependentReactive[A, Reactive.IDENTITY, Reactive.UNIT, Reactive.IDENTITY, EventStream] {
  protected def reevaluatePulse(transaction: Transaction): Option[A]
  protected override def reevaluateValue(transaction: Transaction): Unit = ()
  protected override def reevaluate(transaction: Transaction) = reevaluatePulse(transaction).map { ((), _) }
}