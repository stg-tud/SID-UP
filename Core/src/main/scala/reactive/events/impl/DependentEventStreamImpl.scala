package reactive
package events
package impl

import reactive.impl.DependentReactive

trait DependentEventStreamImpl[A] extends EventStreamImpl[A] with DependentReactive[A] {
  self =>
  override object single extends {
    override protected val impl = self
  } with EventStreamImpl.ViewImpl[A] with DependentReactive.ViewImpl[A]
}
