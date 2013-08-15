package projections.observer

import com.typesafe.scalalogging.slf4j.Logging

trait Client extends Observable[Seq[Int]] with Logging {

	val name = "client"
  var orders = List[Int]()

  def makeOrder(order: Int) = {
    logger.info(s"$name received $order")
    orders ::= order
    notifyObservers(orders)
  }

  def startWorking() = {
    logger.info(s"$name startet working")
    init()
  }

  def init(): Unit
}
