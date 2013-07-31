package projections

import reactive.signals.Signal
import reactive.Lift._

class Management[N](implicit num: Numeric[N]) {
	import num._
	lazy val purchases: Signal[N] = SignalRegistry.retrieve("division/purchases").get.asInstanceOf[Signal[N]]
	lazy val sales: Signal[N] = SignalRegistry.retrieve("division/sales").get.asInstanceOf[Signal[N]]

	lazy val difference: Signal[N] = (minus _)(sales, purchases)
	lazy val panic: Signal[Boolean] = (lt _)(difference, zero : Signal[N])

	def startWorking() {
		SignalRegistry.register("management/difference", difference)
		SignalRegistry.register("management/panic", panic)
		panic.observe(p => if (p) println("management paniced"))
		println("management startet working")
	}
}
