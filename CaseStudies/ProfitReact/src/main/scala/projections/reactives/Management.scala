package projections.reactives

import reactive.NumericLift._
import reactive.remote.RemoteReactives
import reactive.signals.Signal

class Management {
  val purchases: Signal[Int] = RemoteReactives.lookupSignal[Int](projections.purchases)
  val sales: Signal[Int] = RemoteReactives.lookupSignal[Int](projections.sales)

  val difference: Signal[Int] = sales - purchases
  val panic: Signal[Boolean] = difference.map { _ < 0 }

  RemoteReactives.rebind(projections.management, difference)
}
