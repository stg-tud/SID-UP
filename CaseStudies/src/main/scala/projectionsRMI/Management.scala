package projectionsRMI

import com.typesafe.scalalogging.slf4j.Logging

class Management
  extends java.rmi.server.UnicastRemoteObject
  with RemoteObservable[Int]
  with Observable[Int]
  with Observer[Int]
  with Logging {

  lazy val purchases = java.rmi.Naming.lookup("purchases").asInstanceOf[RemoteObservable[Int]]
  lazy val sales = java.rmi.Naming.lookup("sales").asInstanceOf[RemoteObservable[Int]]


  var difference: Int = 0

  def startWorking() {
    purchases.addObserver(this)
    sales.addObserver(this)
  }

  def receive(v: Int) = difference = v
}
