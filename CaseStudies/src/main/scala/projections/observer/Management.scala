package projections.observer

import reactive.signals.RoutableVar
import reactive.Lift._

trait Management extends Observable[Int] with Observer[Message[Int]] {

  val disableTransaction = RoutableVar(false)

  var lastSales = 0
  var lastPurchases = 0
  var hasReceived = ""
  var difference: Int = 0

  def init(): Any
  def deinit(): Any

  def recalcDifference() = {
    difference = lastSales - lastPurchases
    notifyObservers(difference)
  }

  def receive(v: Message[Int]) = {
    v.sender match {
      case "purchases" => lastPurchases = v.value
      case "sales" => lastSales = v.value
    }

    (v.direct || disableTransaction.now) match {
      case true => recalcDifference()
      case false => synchronized {
        if (hasReceived == v.sender) throw new Exception("received from same source twice")
        if (hasReceived == "") hasReceived = v.sender
        else {
          hasReceived = ""
          recalcDifference()
        }
      }
    }

  }
}