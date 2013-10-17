package projections.reactives

import projections.Order
import reactive.remote.RemoteSignal
import reactive.remote.ActorRemoteSignal
import reactive.signals.Signal

class Client(val orders: Signal[Seq[Order]]) {
  ActorRemoteSignal.rebind("client", orders)
}
