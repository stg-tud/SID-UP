package projectionsRMI

import com.typesafe.scalalogging.slf4j.Logging

class Management
  extends java.rmi.server.UnicastRemoteObject
  with RemoteObservable[Int]
  with Observable[Int]
  with Observer[Message[Int]]
  with Logging {

  lazy val purchases = java.rmi.Naming.lookup("purchases").asInstanceOf[RemoteObservable[Message[Int]]]
  lazy val sales = java.rmi.Naming.lookup("sales").asInstanceOf[RemoteObservable[Message[Int]]]

  var lastSales = 0
  var lastPurchases = 0
  var hasReceived = ""
  var difference: Int = 0

  def startWorking() = {
    purchases.addObserver(this)
    sales.addObserver(this)
  }

  def recalcDifference() = {
    difference = lastSales - lastPurchases
    notifyObservers(difference)
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
