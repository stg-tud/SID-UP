package projectionsSockets

import java.io._
import java.net._

import com.typesafe.scalalogging.slf4j.Logging

abstract class Division(val name: String, val port: Int) extends Logging {

  def thread[F](f: => F) = (new Thread( new Runnable() { def run() { f } } )).start

  def total: Int
  //def calculating: Boolean
  def processOrders(orders: Seq[Int])

  lazy val csock = new ObjectInputStream(new Socket("127.0.0.1", 27800).getInputStream())

  var msock: ObjectOutputStream = _

  def startWorking() {
    logger.debug(s"$name startet working")

    thread {
      val serverSocket = new ServerSocket(port)
      logger.debug(s"$name accepting connections on $serverSocket")
      val sock = serverSocket.accept()
      logger.debug(s"$name accepted connection")
      val oos = new ObjectOutputStream(sock.getOutputStream())
      oos.writeObject(Some(total))
      msock = oos
      serverSocket.close()
    }

    thread {
      while (true) {
        logger.debug(s"$name reading object")
        val orders = csock.readObject().asInstanceOf[List[Int]]
        logger.debug(s"$name received $orders")
        processOrders(orders)
        if (msock != null) {
          logger.debug(s"$name writing $total")
          msock.writeObject(Some(total))
        }
      }
    }
  }
}

abstract class OrderSummer(name: String, port: Int) extends Division(name, port) {

  override def total = _total
  var _total: Int = 0

  def processOrders(orders: Seq[Int]): Unit = {
    _total = orders.sum
  }

}

class Purchases(var perOrderCost: Int) extends OrderSummer("purchases", 27801)
{
  override def processOrders(orders: Seq[Int]): Unit = {
    _total = orders.sum + orders.length * perOrderCost
    logger.info(s"$name total is now $total")
  }
}

class Sales extends OrderSummer("sales", 27802) {
  override def processOrders(orders: Seq[Int]): Unit = {
    Thread.sleep(500) // sales is kinda slow …
    super.processOrders(orders)
    _total *= 2
    logger.info(s"$name total is now $total")
  }
}
