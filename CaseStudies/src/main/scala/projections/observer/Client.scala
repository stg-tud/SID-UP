package projections.observer

import projections.Order

trait Client extends Observable[Seq[Order]] {
  val name = "client"
  var orders = Seq[Order]()

  def setOrders(orders: Seq[Order]) = {
    this.orders = orders
    notifyObservers(orders)
  }

  def init(): Any
  def deinit(): Any
}
