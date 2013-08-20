package projections.observer

import projections.Order

trait Division extends Observable[Message[Int]] with Observer[Seq[Order]] {

  val name: String

  def init(): Any
  def deinit(): Any

  var total = 0
  var currentOrders = Seq[Order]()

  override def receive(orders: Seq[Order]) = {
    currentOrders = orders
    total = calculateTotal(orders)
    notifyObservers(Message(total,name))
  }

  def calculateTotal(orders: Seq[Order]): Int
}

trait Purchases extends Division {
  var perOrderCost: Int
  val name = "purchases"
  override def calculateTotal(orders: Seq[Order]) = orders.map{_.value}.sum + orders.length * perOrderCost
  def changeOrderCost(v: Int): Unit = {
    perOrderCost = v
    total = calculateTotal(currentOrders)
    notifyObservers(Message(total, name, direct = true))
  }
}

trait Sales extends Division {
  val name = "sales"
  val sleep: Int
  override def calculateTotal(orders: Seq[Order]) = {
    if (sleep > 0) Thread.sleep(sleep)
    orders.map{_.value}.sum * 2
  }
}
