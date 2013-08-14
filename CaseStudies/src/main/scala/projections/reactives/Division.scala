package projections.reactives

import reactive.events.EventSource
import reactive.events.EventStream
import reactive.Lift._
import reactive.NumericLift._
import reactive.LiftableWrappers._
import reactive.signals.Signal
import reactive.signals.Val
import reactive.signals.Var
import Numeric.Implicits._
import projections.Order


abstract class OrderSummer[N: Numeric](val name: String) {
  lazy val orders = SignalRegistry(s"client/orders").asInstanceOf[Signal[List[Order[N]]]]

  def startWorking() {
    SignalRegistry.register(s"division/$name", total)
    println(s"$name startet working")
    total.observe { newTotal: N => println(s"$name published new total: $newTotal") }
  }
  val calculating = Var(false)

  def total = _total
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

  def calculateCost(orders: List[Order[N]]): N = orders.map{_.value}.sum

}

class Purchases[N: Numeric](perOrderCost: Signal[N]) extends OrderSummer[N]("purchases")
{
  lazy val orderCount = orders.map(os => implicitly[Numeric[N]].fromInt(os.size))
  override lazy val total = orderCount * perOrderCost + super.total
}

class Sales[N](implicit num: Numeric[N]) extends OrderSummer[N]("sales") {
  override def calculateCost(order: List[Order[N]]): N = {
    Thread.sleep(500) // sales is kinda slow …
    val income = super.calculateCost(order)
    income + income
  }
}
