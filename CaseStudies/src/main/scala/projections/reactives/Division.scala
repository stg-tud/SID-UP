package projections.reactives

import Numeric.Implicits._
import projections.Order
import reactive.Lift._
import reactive.LiftableWrappers._
import reactive.NumericLift._
import reactive.remote.RemoteSignal
import reactive.signals.Signal

abstract class Division(val name: String) {
  lazy val orders = RemoteSignal.lookup[Seq[Order]]("client")

  def init() = RemoteSignal.rebind(name, total)

  def total: Signal[Int]
}

class Purchases(perOrderCost: Signal[Int]) extends Division("purchases") {
  lazy val orderCount: Signal[Int] = orders.map { _.size }
  lazy val total = (orderCount * perOrderCost + orders.map { _.map { _.value }.sum })
}

class Sales(val sleep: Int = 0) extends Division("sales") {
  lazy val total = orders.map { o =>
    if (sleep > 0) Thread.sleep(sleep)
    o.map { _.value }.sum * 2
  }
}
