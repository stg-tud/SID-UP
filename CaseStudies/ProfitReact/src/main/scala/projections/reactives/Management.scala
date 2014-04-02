package projections.reactives

import Numeric.Implicits._
import projections.Order
import reactive.Lift._
import reactive.NumericLift._
import reactive.signals.Signal
import reactive.remote.RemoteReactives

class Management {
  val purchases: Signal[Int] = RemoteReactives.lookupSignal[Int](projections.purchases)
  val sales: Signal[Int] = RemoteReactives.lookupSignal[Int](projections.sales)

  val difference: Signal[Int] = sales - purchases
  val panic: Signal[Boolean] = difference.map { _ < 0 }

  RemoteReactives.rebind(projections.management, difference)
}
