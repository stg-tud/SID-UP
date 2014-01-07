package projections.reactives

import reactive.signals.Signal
import reactive.Lift._
import Numeric.Implicits._
import reactive.NumericLift._
import projections.Order

class Management {
  lazy val purchases: Signal[Int] = SignalRegistry.retrieve("purchases").get.asInstanceOf[Signal[Int]]
  lazy val sales: Signal[Int] = SignalRegistry.retrieve("sales").get.asInstanceOf[Signal[Int]]

  lazy val difference: Signal[Int] = sales - purchases
  lazy val panic: Signal[Boolean] = difference.map { _ < 0 }
}
