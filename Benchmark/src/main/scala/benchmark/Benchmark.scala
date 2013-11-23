package benchmark

import org.scalameter.api._
import scala.language.higherKinds
import scala.concurrent.{Await, Promise}
import scala.concurrent.duration.Duration

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

  val repetitions = 10
  val iterations = 10
  val testsize = 25
  val nanobusy = Seq(0L)
  val nanosleep = Seq(0L, 1L, 1000L, 10000L, 100000L, 1000L * 1000L)

  def iterate[T](iterations: Int)(f: Int => T) = {
    var i = 0
    while (i < iterations) {
      f(i)
      i += 1
    }
  }

  val parameters = for {
    e <- Gen.enumeration("nanosleep")(nanosleep: _*)
    d <- Gen.enumeration("nanonbusy")(nanobusy: _*)
    a <- Gen.single("repetitions")(repetitions)
    b <- Gen.single("iterations")(iterations)
    c <- Gen.single("testsize")(testsize)
  } yield (a, b, c, d, e)

  simpleTestGroup("signal chain",
    expected = (testsize, i) => i + testsize,
    "scalareact" -> (new ReactChainBench(_)),
    "playground" -> (new DistChainBench(_)),
    "wrappedplayground" -> (new WrappedChainBench(_, PlaygroundWrapper)),
    "wrappedscalareact" -> (new WrappedChainBench(_, ScalaReactWrapper()))
    //"wrappedscalarx" -> (new WrappedChainBench(_, ScalaRxWrapper))
  )

  simpleTestGroup("signal fan",
    expected = (testsize, i) => (i + 1) * testsize,
    "scalareact" -> (new ReactFanBench(_)),
    "playground" -> (new DistFanBench(_)),
    "wrappedplayground" -> (new WrappedFanBench(_, PlaygroundWrapper)),
    "wrappedscalareact" -> (new WrappedFanBench(_, ScalaReactWrapper()))
    //"wrappedscalarx" -> (new WrappedFanBench(_, ScalaRxWrapper))
  )

  simpleTestGroup("three hosts",
    expected = (testsize, i) => (i + 1001) * testsize + (i + 1000 + testsize) + ( i + 1000),
    "wrappedplayground" -> (new ThreeHosts(_, PlaygroundWrapper)),
    "wrappedscalareact" -> (new ThreeHosts(_, ScalaReactWrapper()))
    //"wrappedscalarx" -> (new ThreeHosts(_, ScalaRxWrapper)),
    //"wrappedscalarxparallel" -> (new ThreeHosts(_, ScalaRxWrapperParallel))
  )

  def simpleTestGroup(name: String, expected: (Int, Int) => Int, tests: Pair[String, Int => SimpleTest]*) =
    performance.of(name).config(
      exec.benchRuns -> repetitions
    ).in {
      tests.foreach { case (name, test) =>
        measure.method(name).in {
          var simpleTest: SimpleTest = null
          using(parameters).beforeTests {
            simpleTest = test(testsize)
            simpleTest.init()
          }.setUp { case (repetitions, iterations, testsize, busytime, sleeptime) =>
            Simulate.nanobusy = busytime
            Simulate.nanosleep = sleeptime
            // manual warmup step …
            simpleTest.run(-1)
            iterate(iterations) { i =>
              val res = simpleTest.run(i)
              assert(expected(testsize, i) == res)
            }
            simpleTest.run(-1)
          }.in { case (_, iterations, _, _, _) =>
            iterate(iterations) { i =>
              val res = simpleTest.run(i)
              assert(expected(testsize, i) == res)
            }
          }
        }
      }
    }

}

object Simulate {
  var nanobusy = 0L
  var nanosleep = 0L

  def apply(nanos: Long = nanobusy): Long = {
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

  def network(nanos: Long = nanosleep): Long = apply(nanos)
}

trait SimpleTest {
  def run(i: Int): Int
  def init(): Unit = {}
}

trait SimpleWaitingTest[GenSig[Int], GenVar[Int] <: GenSig[Int]] extends SimpleTest {

  def wrapper: ReactiveWrapper[GenSig, GenVar]

  def run(i: Int): Int = {
    val done = Promise[Int]()
    runDone = v => { done.success(v)}
    wrapper.setValue(first)(i)
    Await.ready(done.future,Duration.Inf)
    wrapper.getValue(last)
  }

  override def init() = {
    observer
  }

  def first: GenVar[Int]

  def last: GenSig[Int]

  var runDone: Int => Unit = v => ()
  lazy val observer = wrapper.observe(last)(v => runDone(v))


}
