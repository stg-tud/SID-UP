package projections.observer.rmi

import projections.observer.Message
import projections.observer.common.Order


package object common {
  def startRegistry() = java.rmi.registry.LocateRegistry.createRegistry(1099)
}

@remote trait RemoteObservable[I] {
  def addObserver(o: Observer[I]): Unit
}

@remote trait Observer[I] {
  def receive(v: I): Unit
}

trait Observable[I] {
  var observers = List[Observer[I]]()
  def addObserver(o: Observer[I]) = observers ::= o
  def notifyObservers(v: I) = observers.foreach(_.receive(v))
}


class Client extends java.rmi.server.UnicastRemoteObject
with projections.observer.Client with Observable[Seq[Order]] with RemoteObservable[Seq[Order]] {
  override def init(): Unit = java.rmi.Naming.rebind(s"$name", this)
}

abstract class Division extends java.rmi.server.UnicastRemoteObject
with Observable[Message[Int]] with Observer[Seq[Order]] with RemoteObservable[Message[Int]] {
  this: projections.observer.Division =>
  override def init(): Unit = {
    java.rmi.Naming.rebind(s"$name", this)
    val remoteClient = java.rmi.Naming.lookup("client").asInstanceOf[RemoteObservable[Seq[Order]]]
    remoteClient.addObserver(this)
  }
}

class Purchases(var perOrderCost: Int = 5) extends Division with projections.observer.Purchases

class Sales(val sleep: Int = 0) extends Division with projections.observer.Sales

class Management extends java.rmi.server.UnicastRemoteObject
with projections.observer.Management with Observer[Message[Int]] with Observable[Int] {
  def init(): Unit = {
    java.rmi.Naming.rebind(s"management", this)
    val purchases = java.rmi.Naming.lookup("purchases").asInstanceOf[RemoteObservable[Message[Int]]]
    val sales = java.rmi.Naming.lookup("sales").asInstanceOf[RemoteObservable[Message[Int]]]
    purchases.addObserver(this)
    sales.addObserver(this)
  }
}
