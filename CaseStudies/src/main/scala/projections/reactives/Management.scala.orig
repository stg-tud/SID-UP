package projections.reactives

import Numeric.Implicits._
import projections.Order
import reactive.Lift._
import reactive.NumericLift._
import reactive.remote.ActorRemoteSignal
import reactive.signals.Signal

class Management {
  val purchases: Signal[Int] = ActorRemoteSignal.lookup[Int]("purchases")
  val sales: Signal[Int] = ActorRemoteSignal.lookup[Int]("sales")

  val difference: Signal[Int] = sales - purchases
  val panic: Signal[Boolean] = difference.map { _ < 0 }
}
