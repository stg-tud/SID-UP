package projections.reactives

import projections.Order
import reactive.remote.RemoteSignal
import reactive.signals.Signal

class Client(val orders: Signal[Seq[Order]]) {
  RemoteSignal.rebind(projections.client, orders)
}
