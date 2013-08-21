package projections

import org.scalameter.api._
import reactive.signals.Var
import reactive.events.EventSource

object TestReactives {
  import projections.reactives._

  val makeOrder = EventSource[Order]()
  val c = new Client(makeOrder)
  val s = new Sales(0)
  val p = new Purchases(Var(5))
  val m = new Management()

  val order = Order(10)

  c.init()
  p.init()
  s.init()

  def apply(orders: Int) = {(1 to orders).foreach(_ => makeOrder << order); println(s"$orders reactives ${m.difference.now}")}
}


object TestRMI {
  import projections.observer.rmi._

  try {java.rmi.registry.LocateRegistry.createRegistry(1099)}
  catch {case _: Exception => }

  val c = new Client()
  val s = new Sales(0)
  val p = new Purchases(5)
  val m = new Management()

  val order = Order(10)

  c.init()
  p.init()
  s.init()
  m.init()

  def apply(orders: Int) = {(1 to orders).foreach(_ => c.makeOrder(order)); println(s"$orders rmi ${m.difference}")}
}

object TestSockets {
  import projections.observer.sockets._
  val c = new Client()
  val s = new Sales(0)
  val p = new Purchases(5)
  val m = new Management()

  val order = Order(10)

  c.init()
  p.init()
  s.init()
  m.init()

  def apply(orders: Int) = { (1 to orders).foreach(_ => c.makeOrder(order)); println(s"$orders sockets ${m.difference}") }

}


object SimpleBenchmark extends PerformanceTest.Quickbenchmark {
  val sizes = Gen.range("size")(200, 300, 100)

  measure method "reactives" in {
    using(sizes) in { size => projections.reactives.SignalRegistry.clear(); TestReactives(size) }
  }

  measure method "rmi" in {
    using(sizes) in { size => TestRMI(size) }
  }

  // measure method "sockets" in {
  //   using(sizes) in { size => TestSockets(size) }
  // }
}
