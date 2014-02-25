package reactive
package events
package impl

import reactive.impl.DependentReactive

abstract class DependentEventStreamImpl[A] extends EventStreamImpl[A] with DependentReactive[Unit, A]
