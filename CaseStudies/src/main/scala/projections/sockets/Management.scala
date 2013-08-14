package projections.sockets

import java.io._
import java.net._

import com.typesafe.scalalogging.slf4j.Logging

class Management extends Observer[Message[Int]] with Observable[Int] with Logging {

  val port = 27803

  var lastSales = 0
  var lastPurchases = 0
  var hasReceived = ""
  var difference: Int = 0

  def recalcDifference() = {
    difference = lastSales - lastPurchases
    notifyObservers(difference)
  }

  def startWorking() {
    logger.debug("management startet working")
    connect(27801)
    connect(27802)
    startObservable()
  }

  def receive(v: Message[Int]) = {
    v.sender match {
      case "purchases" => lastPurchases = v.value
      case "sales" => lastSales = v.value
    }

    v.direct match {
      case true => recalcDifference()
      case false =>
        if (hasReceived == v.sender) throw new Exception("received from same source twice")
        if (hasReceived == "") hasReceived = v.sender
        else {
          hasReceived = ""
          recalcDifference()
        }
    }

  }
}
