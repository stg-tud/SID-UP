package projections.observer

import reactive.signals.RoutableVar
import reactive.Lift._

class Management extends Observable[Int]("management") {

  val disableTransaction = RoutableVar(false)

  var lastSales = 0
  var lastPurchases = 0
  var difference: Int = 0

  def recalculate() = {
    difference = lastSales - lastPurchases
    publish(difference)
  }

  val salesObserver = new Observer[Int]("sales") {
    def receive(v: Int) = {
      lastSales = v
      update("sales")
    }
  }

  val purchObserver = new Observer[Int]("purchases") {
    def receive(v: Int) = {
      lastPurchases = v
      update("purchases")
    }
  }

  var hasReceived = ""

  def update(sender: String) = {
    if (disableTransaction.now) recalculate()
    else synchronized {
      if (hasReceived == sender) throw new Exception("received from same source twice")
      if (hasReceived == "") hasReceived = sender
      else {
        hasReceived = ""
        recalculate()
      }
    }
  }

}
