package projections

import reactive.events.EventSource
import reactive.events.EventStream
import reactive.Lift._
import reactive.LiftableWrappers._
import reactive.signals.Signal
import reactive.signals.Val
import reactive.signals.Var

abstract class Division[N](val name: String)(implicit num: Numeric[N]) {
  import num._

  def total: Signal[N]
  def calculating: Signal[Boolean]

  lazy val orders = SignalRegistry(s"clients/client1/orderlist").asInstanceOf[Signal[List[Order[N]]]]
  //	lazy val orders: Signal[EventStream[Order[N]]] = {
  //		val clients = EventSourceRegistry("clients").map{_.asInstanceOf[String]}
  //		clients.fold(EventSource[Order[N]](): EventStream[Order[N]])
  //		{ (os, cname) =>
  //			val client = SignalRegistry(s"clients/$cname/orderList").asInstanceOf[Signal[Order[N]]]
  //			os.merge(client)
  //		}
  //	}

  def startWorking() {
    SignalRegistry.register(s"division/$name", total)
    println(s"$name startet working")
  }
}

abstract class OrderSummer[N](name: String)(implicit num: Numeric[N]) extends Division[N](name)(num) {
  override val calculating = Var(false)

  override val total = orders.map { currentList =>
    calculating << true
    try {
      calculateCost(currentList)
    } finally {
      calculating << false
    }
  } //.flatten

  total.observe { newTotal: N => println(s"$name published new total: $newTotal"); }

  //	def transpose[T](los: Seq[Signal[T]]): Signal[Seq[T]] = {
  //		val signalEmptyList: Signal[List[T]] = List[T]()
  //		los.foldRight(signalEmptyList){ case (sig, sol) =>
  //			concat[T](sol, sig)
  //		}
  //	}

  def calculateCost(orders: List[Order[N]]): N = {
    println(s"$name processing updated orders...")
    try {
      orders.foldLeft(num.zero) { (acc: N, order: Order[N]) =>
        num.plus(acc, order.cost)
      }
    } finally {
      println(s"$name done processing.")
    }
  }
}

class Purchases[N](implicit num: Numeric[N]) extends OrderSummer[N]("purchases")(num) {
}

class Sales[N](implicit num: Numeric[N]) extends OrderSummer[N]("sales")(num) {
  override def calculateCost(order: List[Order[N]]): N = {
    Thread.sleep(500) // sales is kinda slow …
    val income = super.calculateCost(order)
    num.plus(income, income);
  }
}
