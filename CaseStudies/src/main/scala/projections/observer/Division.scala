package projections.observer

import projections.Order
import projections.Participant

case class Message(total: Int, direct: Boolean)

abstract class Division(participant: Participant) extends Observable[Message](participant) {

  var total = 0
  var currentOrders = Seq[Order]()

  val clientObserver = new Observer[Seq[Order]](projections.client) {
    def receive(orders: Seq[Order]) = {
      currentOrders = orders
      recalculate(direct = false)
    }
  }

  protected def recalculate(direct: Boolean = false) {
    total = calculateTotal(currentOrders)
    publish(Message(total, direct))
  }

  def sumValues(orders: Seq[Order]) = orders.map { _.value }.sum

  def calculateTotal(orders: Seq[Order]): Int
}

class Purchases(var perOrderCost: Int = 5) extends Division(projections.purchases) {

  override def calculateTotal(orders: Seq[Order]) = sumValues(orders) + orders.length * perOrderCost

  def changeOrderCost(v: Int): Unit = {
    perOrderCost = v
    recalculate(direct = true)
  }
}

class Sales(sleep: Int = 0) extends Division(projections.sales) {

  override def calculateTotal(orders: Seq[Order]) = {
    if (sleep > 0) Thread.sleep(sleep)
    sumValues(orders) * 2
  }
}
