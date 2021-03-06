package projections.observer

import projections.Order

class Client extends Observable[Seq[Order]](projections.client) {
  var orders = Seq[Order]()

  def setOrders(orders: Seq[Order]) = {
    this.orders = orders
    publish(orders)
  }
}
