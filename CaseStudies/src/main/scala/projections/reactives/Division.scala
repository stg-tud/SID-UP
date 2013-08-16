package projections.reactives

import reactive.Lift._
import reactive.NumericLift._
import reactive.LiftableWrappers._
import reactive.signals.Signal
import Numeric.Implicits._

abstract class Division(val name: String) {
  lazy val orders = SignalRegistry(s"client").asInstanceOf[Signal[Seq[Order]]]

  def startWorking() {
    SignalRegistry.register(s"$name", total)
  }

  def total: Signal[Int]
}

class Purchases(perOrderCost: Signal[Int]) extends Division("purchases")
{
  lazy val orderCount: Signal[Int] = orders.map{_.size}
  lazy val total = orderCount * perOrderCost + orders.map{_.map{_.value}.sum}
}

class Sales(val sleep: Int = 0) extends Division("sales") {
  lazy val total = orders.map{ o =>
    if (sleep > 0) Thread.sleep(500) 
    o.map{_.value}.sum * 2
  }
}
