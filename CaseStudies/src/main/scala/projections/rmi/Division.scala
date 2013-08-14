package projections.rmi

import com.typesafe.scalalogging.slf4j.Logging

abstract class Division(val name: String)
  extends java.rmi.server.UnicastRemoteObject
  with RemoteObservable[Message[Int]]
  with Observable[Message[Int]]
  with Observer[Seq[Int]]
  with Logging {

  def startWorking() {
    logger.info(s"$name startet working")
    java.rmi.Naming.rebind(s"$name", this)
    val remoteClient = java.rmi.Naming.lookup("client").asInstanceOf[RemoteObservable[Seq[Int]]]
    remoteClient.addObserver(this)
  }

  var total = 0
  var currentOrders = Seq[Int]()

  override def receive(orders: Seq[Int]) = {
    currentOrders = orders
    total = calculateTotal(orders)
    notifyObservers(Message(total,name))
  }

  def calculateTotal(orders: Seq[Int]): Int
}

class Purchases(var perOrderCost: Int) extends Division("purchases") {
  override def calculateTotal(orders: Seq[Int]) = orders.sum + orders.length * perOrderCost
  def changeOrderCost(v: Int): Unit = {
    perOrderCost = v
    total = calculateTotal(currentOrders)
    notifyObservers(Message(total, name, direct = true))
  }
}

class Sales extends Division("sales") {
  override def calculateTotal(orders: Seq[Int]) = orders.sum * 2
}
