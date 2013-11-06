package benchmark

import org.scalameter.api._
import reactive.signals.impl.FunctionalSignal
import scala.language.higherKinds
import scala.react.Domain

object DistReactBenchmark extends PerformanceTest {

  def measurer = new Measurer.IgnoringGC with Measurer.PeriodicReinstantiation {
    override val defaultFrequency = 12
    override val defaultFullGC = true
  }

  val executor = LocalExecutor(// SeparateJvmsExecutor || LocalExecutor
    new Executor.Warmer.Default,
    Aggregator.complete(Aggregator.average),
    measurer)

  val reporter: Reporter = Reporter.Composite(
    new RegressionReporter(
      RegressionReporter.Tester.Accepter(),
      RegressionReporter.Historian.Complete()),
    HtmlReporter(true),
    new DsvReporter(delimiter = '\t'),
    new LoggingReporter)

  val persistor = new SerializationPersistor("./tmp/")

  val repetitions = 50
  val iterations = 10
  val testsize = 100
  val nanosleep = 100000

  def iterate[T](iterations: Int)(f: Int => T) = {
    var i = 0
    while (i < iterations - 1) {
      f(i)
      i += 1
    }
  }

  val parameters = for {
    a <- Gen.single("repetitions")(repetitions)
    b <- Gen.single("iterations")(iterations)
    c <- Gen.single("testsize")(testsize)
    d <- Gen.single("nanosleep")(nanosleep)
  } yield (a, b, c, d)

  simpleTestGroup("signal chain",
    expected = (testsize, i) => i + testsize,
    "scalareact" -> (new ReactChainBench(_)),
    "playground" -> (new DistChainBench(_)),
    "wrappedplayground" -> (new WrappedChainBench(_, PlaygroundWrapper)),
    "wrappedscalareact" -> (new WrappedChainBench(_, ScalaReactWrapper())),
    "wrappedscalarx" -> (new WrappedChainBench(_, ScalaRxWrapper))
  )

  simpleTestGroup("signal fan",
    expected = (testsize, i) => (i + 1) * testsize,
    "scalareact" -> (new ReactFanBench(_)),
    "playground" -> (new DistFanBench(_)),
    "wrappedplayground" -> (new WrappedFanBench(_, PlaygroundWrapper)),
    "wrappedscalareact" -> (new WrappedFanBench(_, ScalaReactWrapper()))
    //"wrappedscalarx" -> (new WrappedFanBench(_, ScalaRxWrapper))
  )

  def simpleTestGroup(name: String, expected: (Int, Int) => Int, tests: Pair[String, Int => SimpleTest]*) =
    performance.of(name).config(
      exec.benchRuns -> repetitions
    ).in {
      tests.foreach {
        case (name, test) =>
          measure.method(name).in {
            var simpleTest: SimpleTest = null
            using(parameters).beforeTests {
              simpleTest = test(testsize)
            }.in {
              _ =>
                iterate(iterations) {
                  i =>
                    assert(expected(testsize, i) == simpleTest.run(i))
                }
            }
          }
      }
    }

}

object simulateWork {
  def apply(nanos: Long = DistReactBenchmark.nanosleep): Long = {
    if (nanos > 0) {
      val ct = System.nanoTime()
      var res = 0L
      while (nanos > res) {
        res = System.nanoTime() - ct
      }
      res
    }
    else 0L
  }
}

trait SimpleTest {
  def run(i: Int): Int
}


