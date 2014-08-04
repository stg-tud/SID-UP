package projections.observer

import projections.Participant
import reactive.Lift.single._
import reactive.signals.RoutableVar

class Management extends Observable[Int](projections.management) {

  val disableTransaction = RoutableVar(false)

  var lastSales = 0
  var lastPurchases = 0
  var difference: Int = 0

  def recalculate() = {
    difference = lastSales - lastPurchases
    publish(difference)
  }

  val salesObserver = Observer(projections.sales) {
    v: Message =>
      lastSales = v.total
      update(projections.sales, v.direct)
  }

  val purchasesObserver = Observer(projections.purchases) {
    v: Message =>
      lastPurchases = v.total
      update(projections.purchases, v.direct)
  }

  var hasReceived: Option[Participant] = None

  def update(sender: Participant, direct: Boolean) = {
    if (disableTransaction.single.now || direct) recalculate()
    else synchronized { hasReceived match {
      case Some(`sender`) =>
        throw new Exception("received from same source twice")
      case None =>
        hasReceived = Some(sender)
      case Some(_) =>
        hasReceived = None
        recalculate()
    }
  }
  }

}
