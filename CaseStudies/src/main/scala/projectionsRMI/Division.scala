package projectionsRMI

import com.typesafe.scalalogging.slf4j.Logging

abstract class Division(val name: String)
  extends java.rmi.server.UnicastRemoteObject
  with RemoteObservable[Int]
  with Observable[Int]
  with Observer[Seq[Int]]
  with Logging {

  lazy val remoteClient = java.rmi.Naming.lookup("client").asInstanceOf[RemoteObservable[Seq[Int]]]

  def startWorking() {
    logger.info(s"$name startet working")
    java.rmi.Naming.rebind(s"$name", this)
    remoteClient.addObserver(this)
  }

  var total = 0

  override def receive(orders: Seq[Int]) = {
    total = calculateTotal(orders)
    notifyObservers(total)
  }

  def calculateTotal(orders: Seq[Int]): Int
}

class Purchases(var perOrderCost: Int) extends Division("purchases") {
  override def calculateTotal(orders: Seq[Int]) = orders.sum + orders.length * perOrderCost
}

class Sales extends Division("sales") {
  override def calculateTotal(orders: Seq[Int]) = orders.sum * 2
}
