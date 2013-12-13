//package reactive.remote
//
//
//import reactive.Transaction
//import reactive.signals.Signal
//import akka.actor._
//import akka.pattern.ask
//import reactive.signals.impl.SignalImpl
//import reactive.Reactive.Dependant
//import scala.concurrent.{ExecutionContext, Await, Future}
//import scala.concurrent.duration.Duration
//import akka.util.Timeout
//import akka.actor.ActorDSL._
//import java.util.UUID
//import reactive.Transaction
//import scala.Some
//
//
//object ActorRemoteSignal {
//  def props[A](signal: Signal[A]): Props = Props(classOf[ActorRemoteSignal[A]], signal)
//  def apply[A](dependency: ActorSelection)(implicit system: ActorSystem): Signal[A] = new ActorSignalSinkImpl(dependency)
//  def rebind[A](name: String, signal: Signal[A])(implicit system: ActorSystem) = system.actorOf(props(signal),name)
//  def lookup[A](name: String)(implicit system: ActorSystem): Signal[A] = apply[A](system.actorSelection(s"/user/$name"))
//}
//
//sealed trait SignalMessage
//
//case class Update[A](transaction: Transaction, pulse: Option[A], updatedSourceDependencies: Option[Set[java.util.UUID]])
//
//case class Init[A](value: A, sourceDependencies: Set[java.util.UUID])
//
//case class RegisterDependant(transaction: Transaction, dependant: ActorRef) extends SignalMessage
//
//case object Done extends SignalMessage
//
//class ActorRemoteSignal[A](dependency: Signal[A]) extends Actor {
//
//  dependency.addDependant(null, dependant = new Dependant {
//    implicit def ec: ExecutionContext = context.system.dispatcher
//
//    def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = {
//      val futures = dependants.map {
//        dep =>
//          dep.ask(Update(transaction,
//            pulse = if (pulsed) dependency.pulse(transaction) else None,
//            updatedSourceDependencies = if (sourceDependenciesChanged) Some(dependency.sourceDependencies(transaction)) else None))(Timeout(3000))
//            .mapTo[SignalMessage]
//      }
//      Await.result(Future.sequence(futures), Duration.Inf)
//    }
//  })
//
//
//  var dependants: Set[ActorRef] = Set()
//
//  def receive: Actor.Receive = {
//    case RegisterDependant(transaction, dependant) =>
//      dependants += dependant
//      sender ! Init(dependency.value(transaction), dependency.sourceDependencies(transaction))
//    case other => println(s"unknown message $other")
//  }
//}
//
//
//class ActorSignalSinkImpl[A](dependency: ActorSelection)(implicit system: ActorSystem)
//  extends SignalImpl[A] {
//
//  val receiverActor = actor(new Act {
//    become {
//      case Update(transaction, pulse: Option[A], updatedSourceDependencies) =>
//        pulse.foreach(now = _)
//        val sdChanged = updatedSourceDependencies match {
//          case Some(usd) if usd != _sourceDependencies =>
//            _sourceDependencies = usd
//            true
//          case _ => false
//        }
//        doPulse(transaction, sdChanged, pulse)
//        sender ! Done
//      case other => println(s"unknown message $other")
//    }
//  })
//
//  var Init(now: A, _sourceDependencies: Set[UUID]) =
//    Await.result(dependency.ask(RegisterDependant(null, receiverActor))(Timeout(3000)).mapTo[Init[A]], Duration.Inf)
//
//  protected[reactive] def sourceDependencies(transaction: reactive.Transaction): Set[java.util.UUID] = _sourceDependencies
//
//  protected[reactive] def value(transaction: reactive.Transaction): A = now
//}
