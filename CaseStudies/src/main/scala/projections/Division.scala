package projections

import reactive.events.EventSource
import reactive.events.EventStream
import reactive.Lift._
import reactive.LiftableWrappers._
import reactive.signals.Signal
import reactive.signals.Val
import reactive.signals.Var

abstract class Division[N](val name: String)(implicit num: Numeric[N]) {
  import num._

  def total: Signal[N]
  def calculating: Signal[Boolean]

  lazy val orders = SignalRegistry(s"client/orders").asInstanceOf[Signal[List[Order[N]]]]

  def startWorking() {
    SignalRegistry.register(s"division/$name", total)
    println(s"$name startet working")
  }
}

abstract class OrderSummer[N](name: String)(implicit num: Numeric[N]) extends Division[N](name)(num) {
  override val calculating = Var(false)

  override def total = _total
  lazy val _total = orders.map { currentList =>
    calculating << true
    println(s"$name processing updated orders...")
    try {
      calculateCost(currentList)
    } finally {
      println(s"$name done processing.")
      calculating << false
    }
  }
  override def startWorking() {
    super.startWorking()
    total.observe { newTotal: N => println(s"$name published new total: $newTotal"); }
  }

  def calculateCost(orders: List[Order[N]]): N = 
      orders.foldLeft(num.zero) { (acc: N, order: Order[N]) => num.plus(acc, order.cost) }

}

class Purchases[N](perOrderCost: Signal[N])(implicit num: Numeric[N]) extends OrderSummer[N]("purchases")
{
  lazy val orderCount = orders.map(_.foldLeft(num.zero){ (acc, order) => num.plus(acc, num.one) })
  override lazy val total = (num.plus _)((num.times _)(orderCount, perOrderCost), super.total)
}

class Sales[N](implicit num: Numeric[N]) extends OrderSummer[N]("sales") {
  override def calculateCost(order: List[Order[N]]): N = {
    Thread.sleep(500) // sales is kinda slow …
    val income = super.calculateCost(order)
    num.plus(income, income);
  }
}
