package projections

import org.scalameter.api._
import reactive.signals.Var
import reactive.events.EventSource
import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.concurrent.Semaphore


object SimpleBenchmark extends PerformanceTest.Quickbenchmark {
  val sizes = Gen.range("size")(100, 400, 100)

  val sock = TestSockets
  val rmi = TestRMI
  val react = TestReactives

  Thread.sleep(500) // this is to wait for initialisation

  measure method "reactives" in {
    using(sizes) in { size => react(size) }
  }

  measure method "rmi" in {
    using(sizes) in { size => rmi(size) }
  }

  measure method "sockets" in {
    using(sizes) in { size => sock(size) }
  }
}


object TestReactives extends TestCommon {
  import projections.reactives._

  def name = "reactive"

  val makeOrder = Var[Seq[Order]](List())
  val c = new Client(makeOrder)
  val s = new Sales(0)
  val p = new Purchases(Var(5))
  val m = new Management()

  c.init()
  p.init()
  s.init()

  m.difference.observe{_ => nextStep()}

  def test(v: Int) = makeOrder << Seq(Order(v))
}


object TestRMI extends TestCommon {
  import projections.observer.rmi._

  def name = "rmi"

  try {java.rmi.registry.LocateRegistry.createRegistry(1099)}
  catch {case _: Exception => }

  val c = new Client()
  val s = new Sales(0)
  val p = new Purchases(5)
  val m = new Management()

  c.init()
  p.init()
  s.init()
  m.init()

  m.addObserver(new Observer[Int] {
    def receive(v: Int) = nextStep()
  })

  def test(v: Int) = c.setOrders(Seq(Order(v)))
}

object TestSockets extends TestCommon {
  import projections.observer.sockets._

  def name = "sockets"

  val c = new Client()
  val s = new Sales(0)
  val p = new Purchases(5)
  val m = new Management()

  c.init()
  p.init()
  s.init()
  m.init()

  new Observer[Int] {
    connect(27803)
    override def receive(v: Int) = nextStep()
  }

  def test(v: Int) = c.setOrders(Seq(Order(v)))

}

trait TestCommon {
  val sem = new Semaphore(0)

  def name: String

  def apply(orders: Int) = {
    var todo = orders
    while (todo > 0) {
      test(todo)
      todo -= 1
      sem.acquire()
    }
  }

  def nextStep() = {
    sem.release()
  }

  def test(v: Int)
}
