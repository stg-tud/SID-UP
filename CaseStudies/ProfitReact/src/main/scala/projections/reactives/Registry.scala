package projections.reactives

import reactive.Reactive
import reactive.signals.{RoutableVar, Signal}

import scala.language.higherKinds

class Registry[R <: Reactive[_, _]] {
  var reactives = Map[String, R]()

  def register(name: String, reactive: R) = reactives += (name -> reactive)

  def retrieve(name: String): Option[R] = reactives.get(name)

  def apply(name: String): R = retrieve(name).get

  def clear() = reactives = Map[String, R]()
}

//object Registry extends Registry[Any, ({type λ[+X] = Reactive[X, ReactiveNotification[X]]})#λ ]

object SignalRegistry extends Registry[Signal[Any]] {

  override def register(name: String, sig: Signal[Any]): Unit = {
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
