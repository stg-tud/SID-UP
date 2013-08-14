package projections.sockets

import java.io._
import java.net._

import com.typesafe.scalalogging.slf4j.Logging

class Client(val name: String) extends Observable[Seq[Int]] with Logging {

	val port = 27800

  var orders = List[Int]()

  def makeOrder(order: Int) = {
    logger.info(s"$name received $order")
    orders ::= order
    notifyObservers(orders)
  }

  def startWorking() {
    val serverSocket = new ServerSocket()
    logger.debug(s"$name startet working")
    startObservable()
  }
}
