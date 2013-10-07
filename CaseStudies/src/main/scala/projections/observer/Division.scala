package projections.observer

import projections.Order

abstract class Division(name: String) extends Observable[Int](name) {

  var total = 0
  var currentOrders = Seq[Order]()

  val clientObserver = new Observer[Seq[Order]]("client") {
    def receive(orders: Seq[Order]) = {
      currentOrders = orders
      recalculate()
    }
  }

  protected def recalculate() {
    total = calculateTotal(currentOrders)
    publish(total)
  }

  def sumValues(orders: Seq[Order]) = orders.map { _.value }.sum

  def calculateTotal(orders: Seq[Order]): Int
}

class Purchases(var perOrderCost: Int = 5) extends Division("purchases") {

  override def calculateTotal(orders: Seq[Order]) = sumValues(orders) + orders.length * perOrderCost

  def changeOrderCost(v: Int): Unit = {
    perOrderCost = v
    recalculate()
  }
}

class Sales(sleep: Int = 0) extends Division("sales") {

  override def calculateTotal(orders: Seq[Order]) = {
    if (sleep > 0) Thread.sleep(sleep)
    sumValues(orders) * 2
  }
}
