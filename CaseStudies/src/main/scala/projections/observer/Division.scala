package projections.observer

import com.typesafe.scalalogging.slf4j.Logging

trait Division extends Observable[Message[Int]] with Observer[Seq[Int]] with Logging {

  val name: String

  def startWorking() {
    logger.info(s"$name startet working")
    init()
  }

  def init(): Unit

  var total = 0
  var currentOrders = Seq[Int]()

  override def receive(orders: Seq[Int]) = {
    currentOrders = orders
    total = calculateTotal(orders)
    notifyObservers(Message(total,name))
  }

  def calculateTotal(orders: Seq[Int]): Int
}

trait Purchases extends Division {
  var perOrderCost: Int
  val name = "purchases"
  override def calculateTotal(orders: Seq[Int]) = orders.sum + orders.length * perOrderCost
  def changeOrderCost(v: Int): Unit = {
    perOrderCost = v
    total = calculateTotal(currentOrders)
    notifyObservers(Message(total, name, direct = true))
  }
}

trait Sales extends Division {
  val name = "sales"
  val sleep: Int
  override def calculateTotal(orders: Seq[Int]) = {
    if (sleep > 0) Thread.sleep(sleep)
    orders.sum * 2
  }
}
