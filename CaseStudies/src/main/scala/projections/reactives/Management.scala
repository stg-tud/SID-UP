package projections.reactives

import reactive.signals.Signal
import reactive.Lift._
import Numeric.Implicits._
import reactive.NumericLift._
import projections.Order


class Management[N](implicit num: Numeric[N]) {
  import num._

  lazy val purchases: Signal[N] = SignalRegistry.retrieve("division/purchases").get.asInstanceOf[Signal[N]]
  lazy val sales: Signal[N] = SignalRegistry.retrieve("division/sales").get.asInstanceOf[Signal[N]]

  lazy val difference: Signal[N] = sales - purchases
  lazy val panic: Signal[Boolean] = (lt _)(difference, zero : Signal[N])

  def startWorking() {
    panic.observe(p => if (p) println("management paniced"))
    println("management startet working")
  }
}
