package projections

import reactive.signals.Signal
import reactive.LiftableWrappers._
import reactive.Lift._
import reactive.signals.Var
import reactive.events.EventSource
import reactive.signals.RoutableVar
import reactive.events.EventStream

class Client[N: Numeric](val name: String, val makeOrder : EventStream[Order[N]]) {

//	val week: Signal[Int] = SignalRegistry("week").asInstanceOf[Signal[Int]]
//	private var currentWeek = week.now
	
	private def currentOrders: Signal[List[Order[N]]] = makeOrder.log

//	week.observe{w =>
//		if (w > currentWeek) {
//			currentWeek = w
//			currentOrders << foldOrders
//		}
//		else println(s"time is running backwards last week: $currentWeek new week: $w")
//	}

	def startWorking() {
//		EventRegistry.register(s"clients/$name/orders", currentOrders)
		SignalRegistry.register(s"clients/$name/orderlist", currentOrders)
//		EventSourceRegistry("clients") << name
		println(s"$name startet working")
	}
}
