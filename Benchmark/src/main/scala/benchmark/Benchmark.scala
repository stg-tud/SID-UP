package benchmark

import org.scalameter.api._
import scala.util.Random

object Benchmark extends PerformanceTest {

  lazy val measurer = new Measurer.IgnoringGC with Measurer.PeriodicReinstantiation {
    override val defaultFrequency = 12
    override val defaultFullGC = true
  }

  lazy val executor = LocalExecutor( // SeparateJvmsExecutor || LocalExecutor
    new Executor.Warmer.Default,
    Aggregator.average,
    measurer)

  lazy val reporter: Reporter = Reporter.Composite(
    new RegressionReporter(
      RegressionReporter.Tester.Accepter(),
      RegressionReporter.Historian.Complete()),
    HtmlReporter(true),
    new DsvReporter(delimiter = '\t'),
    new GnuplotReporter(),
    new LoggingReporter)

  lazy val persistor = new SerializationPersistor("./tmp/")

  val iterations = 100000

  def iterate[T](iterations: Int)(f: Int => T) = {
    var i = 0
    while (i < iterations) {
      f(i)
      i += 1
    }
  }

  val parameters = Gen.single("iterations")(iterations)

  val graphs = Map[String, BenchmarkGraphTrait](
    "single-source-unoptimized" -> new SingleSourceBenchmarkGraph(UnoptimizedWrapper),
    "single-source-optimized" -> new SingleSourceBenchmarkGraph(SidupWrapper),
    "many-sources-unoptimized" -> new ManySourcesBenchmarkGraph(UnoptimizedWrapper),
    "many-sources-optimized" -> new ManySourcesBenchmarkGraph(SidupWrapper))

  performTest("change only A", Chain)
  performTest("change only B", Fan)
  performTest("change only C", Regular)
  //performTest("change A and B", Chain, Fan)
  //performTest("change A and C", Chain, Regular)
  //performTest("change B and C", Fan, Regular)
  performTest("change all", Chain, Fan, Regular)

  def performTest(testName: String, sources: Source*) =
    performance.of(testName.replace(' ', '_')).in {

      graphs.foreach {
        case (name, graph) =>
          def runTest(value: Int): Unit = {
            graph.set(sources.map(_ -> value): _*)
            assert(graph.validateResult, graph.state)
          }

          measure.method(name).in {
            using(parameters).beforeTests {
              graph.reset()
            }.in {
              case (iterations) =>
                iterate(iterations) { runTest(_) }
            }
          }
      }
    }

}

