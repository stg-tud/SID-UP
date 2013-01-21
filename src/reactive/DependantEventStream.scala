package reactive

import scala.collection.immutable.Map

abstract class DependantEventStream[A](name : String) extends EventStreamImpl[A](name) with ReactiveDependant[Any] {
	def notifyEvent(event : Event) = notifyDependants(event)
}