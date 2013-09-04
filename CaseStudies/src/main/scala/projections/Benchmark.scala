package projections

import org.scalameter.api._
import reactive.signals.Var
import reactive.events.EventSource
import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.concurrent.Semaphore
import com.typesafe.scalalogging.slf4j._

object SimpleBenchmark extends PerformanceTest {

  override val executor = LocalExecutor( //SeparateJvmsExecutor(
    new Executor.Warmer.Default,
    Aggregator.complete(Aggregator.average),
    new Measurer.IgnoringGC /*with Measurer.PeriodicReinstantiation*/ with Measurer.OutlierElimination)
  // override val reporter = new LoggingReporter //ChartReporter(ChartFactory.XYLine())
  // lazy val reporter: Reporter = HtmlReporter(true)
  def reporter: Reporter = Reporter.Composite(
    // new RegressionReporter(
    // RegressionReporter.Tester.OverlapIntervals(),
    // RegressionReporter.Historian.ExponentialBackoff() ),
    HtmlReporter(false),
    //ChartReporter(ChartFactory.XYLine()),
    ChartReporter(ChartFactory.TrendHistogram()),
    ChartReporter(ChartFactory.NormalHistogram()),
    new LoggingReporter)

  lazy val persistor = Persistor.None

  val orderLists = for {
    size <- Gen.exponential("size")(10, 13000, 2)
  } yield 1 to size map (Order(_))

  val netTypes = Gen.enumeration("net type")(TestRMI, TestReactives, TestSockets)

  val inputs = Gen.tupled(netTypes, orderLists)

  println("waiting for init")
  Thread.sleep(500) // this is to wait for initialisation
  println("init done")

  measure method "rmi" in {
    using(orderLists) config (
      exec.benchRuns -> 50
    ) in {
        case (orders) =>
          TestRMI(orders)
          TestRMI(orders.tail)
      }
  }
  measure method "reactives" in {
    using(orderLists) config (
      exec.benchRuns -> 50
    ) in {
        case (orders) =>
          TestReactives(orders)
          TestReactives(orders.tail)
      }
  }
  measure method "sockets" in {
    using(orderLists) config (
      exec.benchRuns -> 50
    ) in {
        case (orders) =>
          TestSockets(orders)
          TestSockets(orders.tail)
      }
  }
  measure method "pure calculation" in {
    using(orderLists) config (
      exec.benchRuns -> 50
    ) in {
        case (orders) =>
          orders.map { _.value }.sum +
          orders.tail.map{_.value}.sum +
          orders.map { _.value }.sum +
          orders.tail.map{_.value}.sum
      }
  }
}

object TestReactives extends InitReactives {
  def test(orders: Seq[Order]) = setOrders << orders
}

object TestRMI extends InitRMI {
  def test(orders: Seq[Order]) = c.setOrders { orders }
}

object TestSockets extends InitSockets {
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
}

trait InitRMI extends TestCommon {
  import projections.observer.rmi._

  def name = "rmi"

  try { java.rmi.registry.LocateRegistry.createRegistry(1099) }
  catch { case _: Exception => }

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

  new Observer[Int] {
    connect(27803)
    override def receive(v: Int) = done(v)
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
