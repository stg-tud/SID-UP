package projections.observer

import projections.Order
import scala.concurrent._
import ExecutionContext.Implicits.global

package object rmi {
  def startRegistry() = java.rmi.registry.LocateRegistry.createRegistry(1099)
}

package rmi {

  @remote trait RemoteObservable[I] {
    def addObserver(o: Observer[I]): Unit
  }

  @remote trait Observer[I] {
    def receive(v: I): Unit
  }

  trait Observable[I] {
    var observers = List[Observer[I]]()
    def addObserver(o: Observer[I]) = observers ::= o
    def notifyObservers(v: I) = observers.foreach(obs => future { obs.receive(v) })
  }

  class Client extends java.rmi.server.UnicastRemoteObject
      with projections.observer.Client with Observable[Seq[Order]] with RemoteObservable[Seq[Order]] {
    override def init(): Unit = java.rmi.Naming.rebind(name, this)
    override def deinit(): Unit = java.rmi.Naming.unbind(name)
  }

  abstract class Division extends java.rmi.server.UnicastRemoteObject
      with Observable[Message[Int]] with Observer[Seq[Order]] with RemoteObservable[Message[Int]] {
    this: projections.observer.Division =>
    override def init(): Unit = {
      java.rmi.Naming.rebind(name, this)
      val remoteClient = java.rmi.Naming.lookup("client").asInstanceOf[RemoteObservable[Seq[Order]]]
      remoteClient.addObserver(this)
    }
    override def deinit() = java.rmi.Naming.unbind(name)
  }

  class Purchases(var perOrderCost: Int = 5) extends Division with projections.observer.Purchases

  class Sales(val sleep: Int = 0) extends Division with projections.observer.Sales

  class Management extends java.rmi.server.UnicastRemoteObject
      with projections.observer.Management with Observer[Message[Int]] with Observable[Int] {
    def init(): Unit = {
      java.rmi.Naming.rebind("management", this)
      val purchases = java.rmi.Naming.lookup("purchases").asInstanceOf[RemoteObservable[Message[Int]]]
      val sales = java.rmi.Naming.lookup("sales").asInstanceOf[RemoteObservable[Message[Int]]]
      purchases.addObserver(this)
      sales.addObserver(this)
    }
    def deinit() = java.rmi.Naming.unbind("management")
  }

}
