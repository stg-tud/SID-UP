package projectionsSockets

import java.io._
import java.net._

import com.typesafe.scalalogging.slf4j.Logging

class Client(val name: String) extends Logging {

  def thread[F](f: => F) = (new Thread( new Runnable() { def run() { f } } )).start

  var orders = List[Int]()
  var listeners = List[ObjectOutputStream]()

  def makeOrder(order: Int) = synchronized {
    logger.info(s"$name received $order")
    orders ::= order
    listeners.foreach{_.writeObject(orders)}
  }

  def startWorking() {
    val serverSocket = new ServerSocket(27800)
    logger.debug(s"$name startet working")
    thread {
      while (true) {
        logger.debug(s"$name accepting connections")
        val sock = serverSocket.accept()
        logger.debug(s"$name acceptet connection")
        val oos = new ObjectOutputStream(sock.getOutputStream())
        oos.writeObject(orders)
        synchronized { listeners ::= oos }
      }
    }
  }
}
