package reactive
package events
package impl

import reactive.impl.DependentReactive

abstract class DependentEventStreamImpl[P] extends EventStreamImpl[P] with DependentReactive[P]
