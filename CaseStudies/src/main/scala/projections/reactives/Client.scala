package projections.reactives

import reactive.signals.Signal
import reactive.LiftableWrappers._
import reactive.Lift._
import reactive.signals.Var
import reactive.events.EventSource
import reactive.signals.RoutableVar
import reactive.events.EventStream
import projections.Order


class Client(val makeOrder : EventStream[Order]) {
  def currentOrders: Signal[List[Order]] = makeOrder.log

  def startWorking() {
    SignalRegistry.register("client", currentOrders)
  }
}
