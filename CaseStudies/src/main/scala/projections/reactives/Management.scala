package projections.reactives

import Numeric.Implicits._
import projections.Order
import reactive.Lift._
import reactive.NumericLift._
import reactive.remote.RemoteSignal
import reactive.signals.Signal

class Management {
  lazy val purchases: Signal[Int] = RemoteSignal.lookup[Int]("purchases")
  lazy val sales: Signal[Int] =  RemoteSignal.lookup[Int]("sales")

  lazy val difference: Signal[Int] = sales - purchases
  lazy val panic: Signal[Boolean] = difference.map { _ < 0 }
}
