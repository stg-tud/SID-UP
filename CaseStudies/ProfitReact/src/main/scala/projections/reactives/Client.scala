package projections.reactives

import projections.Order
import reactive.remote.RemoteReactives
import reactive.signals.Signal

class Client(val orders: Signal[Seq[Order]]) {
  RemoteReactives.rebind(projections.client, orders)
}
