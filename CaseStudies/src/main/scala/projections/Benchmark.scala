package projections

import org.scalameter.api._
import reactive.signals.Var
import reactive.events.EventSource
import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.concurrent.Semaphore
import com.typesafe.scalalogging.slf4j._

object SimpleBenchmark extends PerformanceTest {

  def measurer = new Measurer.IgnoringGC with Measurer.PeriodicReinstantiation {
    override val defaultFrequency = 12
    override val defaultFullGC = true
  }

  val executor = LocalExecutor( // SeparateJvmsExecutor || LocalExecutor
    new Executor.Warmer.Default,
    Aggregator.complete(Aggregator.average),
    measurer)

  val reporter: Reporter = Reporter.Composite(
    new RegressionReporter(
      RegressionReporter.Tester.OverlapIntervals(),
      RegressionReporter.Historian.Complete()),
    HtmlReporter(true),
    ChartReporter(ChartFactory.XYLine()),
    ChartReporter(ChartFactory.TrendHistogram()),
    ChartReporter(ChartFactory.NormalHistogram()),
    new DsvReporter(delimiter = '\t'),
    new LoggingReporter)

  val persistor = new SerializationPersistor("./tmp/")

  val orderLists = for {
    iterations <- Gen.exponential("iterations")(10, 320, 2)
    size <- Gen.exponential("size")(10, 320, 2)
  } yield (iterations, 1 to size map (Order(_)))

  val repetitions = 5
  //var iterations = 100

  def iterate[T](iterations: Int)(f: => T) = {
    var i = 0
    while (i < iterations) {
      f
      i += 1
    }
  }

  measure method "rmi" in {
    var rmi: TestRMI = null
    using(orderLists) beforeTests {
      rmi = new TestRMI()
    } afterTests {
      rmi.deinit()
    } config (
      exec.benchRuns -> repetitions
    ) in {
        case (iterations, orders) =>
          iterate(iterations) {
            rmi(orders)
            rmi(orders.tail)
          }
      }
  }
  measure method "reactives" in {
    var react: TestReactives = null
    using(orderLists) beforeTests {
      react = new TestReactives()
    } config (
      exec.benchRuns -> repetitions
    ) in {
        case (iterations, orders) =>
          iterate(iterations) {
            react(orders)
            react(orders.tail)
          }
      }
  }
  measure method "sockets" in {
    var sockets: TestSockets = null
    using(orderLists) beforeTests {
      sockets = new TestSockets()
    } afterTests {
      sockets.deinit()
    } config (
      exec.benchRuns -> repetitions
    ) in {
        case (iterations, orders) =>
          iterate(iterations) {
            sockets(orders)
            sockets(orders.tail)
          }
      }
  }
  measure method "pure calculation" in {
    using(orderLists) config (
      exec.benchRuns -> repetitions
    ) in {
        case (iterations, orders) =>
          iterate(iterations) {
            orders.map { _.value }.sum +
              orders.tail.map { _.value }.sum +
              orders.map { _.value }.sum +
              orders.tail.map { _.value }.sum
          }

      }
  }
}

class TestReactives extends InitReactives {
  def test(orders: Seq[Order]) = setOrders << orders
}

class TestRMI extends InitRMI {
  def test(orders: Seq[Order]) = c.setOrders { orders }
}

class TestSockets extends InitSockets {
  def test(orders: Seq[Order]) = c.setOrders { orders }
}

trait InitReactives extends TestCommon {
  import projections.reactives._

  def name = "reactive"

  val setOrders = Var[Seq[Order]](List())
  val c = new Client(setOrders)
  val s = new Sales(0)
  val p = new Purchases(Var(perOrderCost))
  val m = new Management()

  c.init()
  p.init()
  s.init()

  m.difference.observe { v => done(v) }

  println("done")
}

trait InitRMI extends TestCommon {
  import projections.observer.rmi._

  def name = "rmi"

  val registry = java.rmi.registry.LocateRegistry.createRegistry(1099)

  val c = new Client()
  val s = new Sales(0)
  val p = new Purchases(perOrderCost)
  val m = new Management()

  c.init()
  p.init()
  s.init()
  m.init()

  m.addObserver(new Observer[Int] {
    def receive(v: Int) = done(v)
  })

  println("done")

  def deinit() = {
    println(s"deinit $name")
    m.deinit()
    p.deinit()
    s.deinit()
    c.deinit()
    registry.list().foreach { name => println(s"unbind $name"); registry.unbind(name) }
    java.rmi.server.UnicastRemoteObject.unexportObject(registry, true);
    println("done")
  }
}

trait InitSockets extends TestCommon {
  import projections.observer.sockets._

  def name = "sockets"

  val c = new Client()
  val s = new Sales(0)
  val p = new Purchases(perOrderCost)
  val m = new Management()

  c.init()
  p.init()
  s.init()
  m.init()
  Thread.sleep(1000) // this is to wait for initialisation

  new Observer[Int] {
    connect(27803)
    override def receive(v: Int) = done(v)
  }

  println("done")

  def deinit() = {
    println(s"deinit $name")
    m.deinit()
    p.deinit()
    s.deinit()
    c.deinit()
    println("done")
  }
}

trait TestCommon extends Logging {
  val sem = new Semaphore(0)

  def name: String

  var result: Int = 0

  val perOrderCost = 5

  override def toString = name

  println(s"initialize object $name")

  def apply(orders: Seq[Order]) = {
    // logger.info(s"test $name ${orders.size}")
    test(orders)
    // logger.info(s"await result")
    sem.acquire()
    // logger.info(s"result is ${result}")
    assert(orders.map { _.value }.sum - perOrderCost * orders.size == result)
  }

  def done(res: Int) = {
    result = res
    sem.release()
  }

  def test(v: Seq[Order])
}
