package projections.reactives

import Numeric.Implicits._
import projections.Order
import reactive.Lift._
import reactive.NumericLift._
import reactive.remote.RemoteSignal
import reactive.signals.Signal

class Management {
  val purchases: Signal[Int] = RemoteSignal.lookup[Int]("purchases")
  val sales: Signal[Int] = RemoteSignal.lookup[Int]("sales")

  val difference: Signal[Int] = sales - purchases
  val panic: Signal[Boolean] = difference.map { _ < 0 }
}
