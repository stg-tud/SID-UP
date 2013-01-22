package reactive

import scala.collection.immutable.Map

abstract class EventStreamImpl[A](name : String) extends ReactiveImpl[A](name) with EventStream[A] {
}