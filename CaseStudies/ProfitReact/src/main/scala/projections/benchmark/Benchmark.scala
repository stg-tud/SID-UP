package projections.benchmark

import org.scalameter.api._
import projections._

object SimpleBenchmark extends PerformanceTest {

  def measurer = new Measurer.IgnoringGC with Measurer.PeriodicReinstantiation {
    override val defaultFrequency = 12
    override val defaultFullGC = true
  }

  val executor = LocalExecutor( // SeparateJvmsExecutor || LocalExecutor
    new Executor.Warmer.Default,
    Aggregator.average,
    measurer)

  val reporter: Reporter = Reporter.Composite(
    new RegressionReporter(
      RegressionReporter.Tester.OverlapIntervals(),
      RegressionReporter.Historian.Complete()),
    HtmlReporter(embedDsv = true),
    new DsvReporter(delimiter = '\t'),
    new GnuplotReporter(),
    new LoggingReporter)

  val persistor = new SerializationPersistor("./tmp/")

  val orderLists = for {
//    size <- Gen.exponential("size")(10, 320, 2)
    iterations <- Gen.range("iterations")(1, 1001, 100)
  } yield (iterations, 1 to 10 map (i => Order(i * iterations)))

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
//  measureNetwork("pure_calculation", () => new TestPureCalculation())

}

//class TestPureCalculation extends TestCommon {
//  def name = "pure calculation"
//  def test(orders: Seq[Order]) = done(orders.map { _.value }.sum * 2 - orders.map { _.value }.sum - orders.size * 5)
//}

class TestReactives extends InitReactives {
  def test(orders: Seq[Order]) = setOrders << orders
}

class TestRMI extends InitRMI {
  def test(orders: Seq[Order]) = c.setOrders { orders }
}

