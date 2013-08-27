package projections.reactives

import reactive.signals.Signal
import projections.Order

class Client(val orders: Signal[Seq[Order]]) {
  def init() = SignalRegistry.register("client", orders)
}
