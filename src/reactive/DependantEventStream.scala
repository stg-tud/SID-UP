package reactive

import scala.collection.immutable.Map

abstract class DependantEventStream[A](name : String) extends ReactiveImpl[A](name) with EventStream[A] with ReactiveDependant[Any] {
	def notifyEvent(event : Event) = notifyDependants(event)
}