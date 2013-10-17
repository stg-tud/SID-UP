package projections.reactives

import Numeric.Implicits._
import projections.Order
import reactive.Lift._
import reactive.LiftableWrappers._
import reactive.NumericLift._
import reactive.remote.ActorRemoteSignal
import reactive.signals.Signal

abstract class Division {
  val orders = ActorRemoteSignal.lookup[Seq[Order]]("client")
  def sumValues(orders: Seq[Order]) = orders.map { _.value }.sum
}

class Purchases(perOrderCost: Signal[Int]) extends Division {
  val orderCount: Signal[Int] = orders.map { _.size }
  val total = (orderCount * perOrderCost + orders.map { sumValues })

  ActorRemoteSignal.rebind("purchases", total)
}

class Sales(val sleep: Int = 0) extends Division {
  val total = orders.map { o =>
    if (sleep > 0) Thread.sleep(sleep)
    sumValues(o) * 2
  }

  ActorRemoteSignal.rebind("sales", total)
}
