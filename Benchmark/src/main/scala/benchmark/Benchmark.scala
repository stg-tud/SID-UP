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

  val iterations = 10000

  def iterate[T](iterations: Int)(f: Int => T) = {
    var i = 0
    while (i < iterations) {
      f(i)
      i += 1
    }
  }

  val parameters = Gen.single("iterations")(iterations)

  val graphs = Map[String, BenchmarkGraphTrait](
    "optimized" -> new BenchmarkGraph(SidupWrapper),
    "unoptimized" -> new BenchmarkGraph(UnoptimizedWrapper))

  performTest("change only A", SourceA)
  performTest("change only B", SourceB)
  performTest("change only C", SourceC)
  performTest("change A and B", SourceA, SourceB)
  performTest("change A and C", SourceA, SourceC)
  performTest("change B and C", SourceB, SourceC)
  performTest("change all", SourceA, SourceB, SourceC)

  def performTest(testName: String, sources: Source*) =
    performance.of(testName.replace(' ', '_')).config( //      exec.benchRuns -> repetitions,
    /*exec.maxWarmupRuns -> 4*/ ).in {

      graphs.foreach {
        case (name, graph) =>
          def runTest(value: Int): Unit = {
            graph.set(sources.map(_ -> value): _*)
//            Thread.sleep(10)
            assert(graph.validateResult, graph.state)
          }

          measure.method(name).in {
            using(parameters).beforeTests {
              graph.reset()
              println(s"before test $testName $name: " + graph.state)
            } /*.setUp {
                case (iterations) =>
                  // manual warmup step …
                  runTest(-42)
                  iterate(iterations) { runTest(_) }
                  runTest(-84)
              }*/ .in {
                case (iterations) =>
                  iterate(iterations) { runTest(_) }
              }
          }
      }
    }

}

