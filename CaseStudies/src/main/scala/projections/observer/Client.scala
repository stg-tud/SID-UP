package projections.observer

import projections.Order

trait Client extends Observable[Seq[Order]] {
  val name = "client"
  var orders = List[Order]()

  def makeOrder(order: Order) = {
    orders ::= order
    notifyObservers(orders)
  }

  def init(): Any
  def deinit(): Any
}
