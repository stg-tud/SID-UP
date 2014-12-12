package projections.reactives

import projections.Order
import reactive.NumericLift._
import reactive.remote.RemoteReactives
import reactive.signals.Signal

abstract class Division {
  val orders = RemoteReactives.lookupSignal[Seq[Order]](projections.client)
  def sumValues(orders: Seq[Order]) = orders.map { _.value }.sum
}

class Purchases(perOrderCost: Signal[Int]) extends Division {
  val orderCount: Signal[Int] = orders.map { _.size }
  val total = orderCount * perOrderCost + orders.map(sumValues)

  RemoteReactives.rebind(projections.purchases, total)
}

class Sales(val sleep: Int = 0) extends Division {
  val total = orders.map { o =>
    if (sleep > 0) Thread.sleep(sleep)
    sumValues(o) * 2
  }

  RemoteReactives.rebind(projections.sales, total)
}
