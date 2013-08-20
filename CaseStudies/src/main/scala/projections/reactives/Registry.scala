package projections.reactives

import reactive.events.EventSource
import reactive.events.EventStream
import reactive.Reactive
import reactive.signals.RoutableVar
import reactive.signals.Signal
import scala.language.higherKinds
import projections.Order


class Registry[V, R[+V] <: Reactive[_,_,_]] {
  var reactives = Map[String, R[V]]()

  def register[T <: V](name: String, reactive: R[T]) {
    reactives += (name -> reactive)
  }

  def retrieve(name: String): Option[R[V]] = {
    reactives.get(name)
  }

  def apply(name: String): R[V] = {
    retrieve(name).get
  }

  def clear() = reactives = Map[String, R[V]]()
}

//object Registry extends Registry[Any, ({type λ[+X] = Reactive[X, ReactiveNotification[X]]})#λ ]

object SignalRegistry extends Registry[Any, Signal] {

  override def register[T](name: String, sig: Signal[T]) {
    reactives.get(name) match {
      case Some(router) => router.asInstanceOf[RoutableVar[Any]] << sig
      case None => reactives += (name -> RoutableVar(sig))
    }
  }
}

// object EventRegistry extends Registry[Any, EventStream]

// object EventSourceRegistry extends Registry[Any, EventStream] {

//   override def apply(name: String): EventSource[Any] = {
//     retrieve(name) match {
//       case Some(source: EventSource[Any]) => source
//       case _ => {
//         val newSource = EventSource[Any]()
//         register(name, newSource)
//         newSource
//       }
//     }
//   }
// }
