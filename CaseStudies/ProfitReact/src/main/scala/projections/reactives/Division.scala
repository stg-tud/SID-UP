package projections.reactives

import Numeric.Implicits._
import projections.Order
import reactive.Lift.single._
import reactive.LiftableWrappers._
import reactive.NumericLift.single._
import reactive.signals.Signal
import reactive.remote.RemoteReactives

abstract class Division {
  val orders = RemoteReactives.lookupSignal[Seq[Order]](projections.client)
  def sumValues(orders: Seq[Order]) = orders.map { _.value }.sum
}

class Purchases(perOrderCost: Signal[Int]) extends Division {
  val orderCount: Signal[Int] = orders.single.map { _.size }
  val total = (orderCount * perOrderCost + orders.single.map { sumValues })

  RemoteReactives.rebind(projections.purchases, total)
}

class Sales(val sleep: Int = 0) extends Division {
  val total = orders.single.map { o =>
    if (sleep > 0) Thread.sleep(sleep)
    sumValues(o) * 2
  }

  RemoteReactives.rebind(projections.sales, total)
}
