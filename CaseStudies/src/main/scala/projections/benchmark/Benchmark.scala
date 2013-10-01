package projections.benchmark

import org.scalameter.api._
import reactive.signals.Var
import reactive.events.EventSource
import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.concurrent.Semaphore
import com.typesafe.scalalogging.slf4j._
import projections._

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
    new DsvReporter(delimiter = '\t'),
    new GnuplotReporter(),
    new LoggingReporter)

  val persistor = new SerializationPersistor("./tmp/")

  val orderLists = for {
    size <- Gen.exponential("size")(10, 320, 2)
    iterations <- Gen.exponential("iterations")(10, 320, 2)
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

  def measureNetwork(name: String, testGen: () => TestCommon) =
    measure method name in {
      var toTest: TestCommon = null
      using(orderLists) beforeTests {
        toTest = testGen()
      } afterTests {
        toTest.deinit()
      } config (
        exec.benchRuns -> repetitions
      ) in {
          case (iterations, orders) =>
            iterate(iterations) {
              toTest(orders)
              toTest(orders.tail)
            }
        }
    }

  measureNetwork("rmi", () => new TestRMI())
  measureNetwork("reactives", () => new TestReactives())
  // measureNetwork("sockets", () => new TestSockets())
  measureNetwork("pure_calculation", () => new TestPureCalculation())

}

class TestPureCalculation extends TestCommon {
  def name = "pure calculation"
  def test(orders: Seq[Order]) = done(orders.map { _.value }.sum * 2 - orders.map { _.value }.sum - orders.size * 5)
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

