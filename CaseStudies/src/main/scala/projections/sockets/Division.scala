package projections.sockets

import java.io._
import java.net._

import com.typesafe.scalalogging.slf4j.Logging

abstract class Division(val name: String, val port: Int) extends Observer[Seq[Int]] with Observable[Message[Int]] with Logging {

  def total: Int = _total
  def processOrders(orders: Seq[Int])

	def receive(orders: Seq[Int]): Unit = {
		currentOrders = orders
		processOrders(orders)
		notifyObservers(Message(total,name))
	}

  def startWorking() {
    logger.debug(s"$name startet working")
    connect(27800)
    startObservable()
  }

  var currentOrders = Seq[Int]()

  var _total: Int = 0
}

class Purchases(var perOrderCost: Int) extends Division("purchases", 27801)
{
  override def processOrders(orders: Seq[Int]): Unit = {
    _total = orders.sum + orders.length * perOrderCost
  }
  def changeOrderCost(v: Int): Unit = {
    perOrderCost = v
    processOrders(currentOrders)
    notifyObservers(Message(total, name, direct = true))
  }
}

class Sales extends Division("sales", 27802) {
  override def processOrders(orders: Seq[Int]): Unit = {
    Thread.sleep(500) // sales is kinda slow …
    _total = orders.sum * 2
  }
}
