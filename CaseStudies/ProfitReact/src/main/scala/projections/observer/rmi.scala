package projections.observer

import projections.Order
import projections.Participant
import scala.concurrent._
import ExecutionContext.Implicits.global
import java.rmi.server.UnicastRemoteObject

@remote trait RemoteObservable[I] {
  def addObserver(o: RemoteObserver[I]): Unit
}

@remote trait RemoteObserver[I] {
  def receive(v: I): Unit
}

class Observable[I](participant: Participant) extends UnicastRemoteObject with RemoteObservable[I] {
  var observers = List[RemoteObserver[I]]()
  def addObserver(o: RemoteObserver[I]) = observers ::= o
  def publish(v: I): Unit = observers.foreach(obs => Future { obs.receive(v) })
  java.rmi.Naming.rebind(participant.name, this)
}

abstract class Observer[I](observing: Participant) extends UnicastRemoteObject with RemoteObserver[I] {
  val observable = java.rmi.Naming.lookup(observing.name).asInstanceOf[RemoteObservable[I]]
  observable.addObserver(this)
}

object Observer {
  def apply[I](p: Participant)(f: I => Unit) = new Observer[I](p) {
    def receive(v: I) = f(v)
  }
}
