package projectionsRMI

import java.rmi.registry.LocateRegistry

import com.typesafe.scalalogging.slf4j.Logging

class Client(val name: String)
  extends java.rmi.server.UnicastRemoteObject
  with Observable[Seq[Int]]
  with RemoteObservable[Seq[Int]]
  with Logging {

  var orders = List[Int]()

  def makeOrder(order: Int) = {
    logger.info(s"$name received $order")
    orders ::= order
    notifyObservers(orders)
  }

  def startWorking() = {
    logger.info(s"$name startet working")
    java.rmi.Naming.rebind(s"$name", this)
  }
}
