package projections.observer

import com.typesafe.scalalogging.slf4j.Logging
import projections.Order

trait Client extends Observable[Seq[Order]] with Logging {

	val name = "client"
  var orders = List[Order]()

  def makeOrder(order: Order) = {
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
