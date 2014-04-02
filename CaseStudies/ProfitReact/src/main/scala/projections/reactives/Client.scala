package projections.reactives

import projections.Order
import reactive.signals.Signal
import reactive.remote.RemoteReactives

class Client(val orders: Signal[Seq[Order]]) {
  RemoteReactives.rebind(projections.client, orders)
}
