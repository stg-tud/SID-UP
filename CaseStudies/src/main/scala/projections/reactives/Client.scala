package projections.reactives

import reactive.signals.Signal
import reactive.LiftableWrappers._
import reactive.Lift._
import reactive.signals.Var
import reactive.events.EventSource
import reactive.signals.RoutableVar
import reactive.events.EventStream
import projections.Order

class Client[N: Numeric](val name: String, val makeOrder : EventStream[Order[N]]) {

  private def currentOrders: Signal[List[Order[N]]] = makeOrder.log

  def startWorking() {
    SignalRegistry.register("client/orders", currentOrders)
    println(s"$name startet working")
  }
}
