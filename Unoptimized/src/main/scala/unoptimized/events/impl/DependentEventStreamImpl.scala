package unoptimized
package events
package impl

import unoptimized.impl.DependentReactive

abstract class DependentEventStreamImpl[P] extends EventStreamImpl[P] with DependentReactive[P]
