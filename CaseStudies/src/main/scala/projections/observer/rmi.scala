package projections.observer

import projections.Order
import scala.concurrent._
import ExecutionContext.Implicits.global
import java.rmi.server.UnicastRemoteObject

@remote trait RemoteObservable[I] {
  def addObserver(o: RemoteObserver[I]): Unit
}

@remote trait RemoteObserver[I] {
  def receive(v: I): Unit
}

class Observable[I](name: String) extends UnicastRemoteObject with RemoteObservable[I] {
  var observers = List[RemoteObserver[I]]()
  def addObserver(o: RemoteObserver[I]) = observers ::= o
  def publish(v: I): Unit = observers.foreach(obs => future { obs.receive(v) })
  java.rmi.Naming.rebind(name, this)
}

abstract class Observer[I](observing: String) extends UnicastRemoteObject with RemoteObserver[I] {
  val observable = java.rmi.Naming.lookup(observing).asInstanceOf[RemoteObservable[I]]
  observable.addObserver(this)
}
