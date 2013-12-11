package benchmark

import org.scalameter.api._
import benchmark.networks._
import scala.util.Random

object Benchmark extends PerformanceTest {

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
    new GnuplotReporter(),
    new LoggingReporter)

  val persistor = new SerializationPersistor("./tmp/")

  val repetitions = 10
  val iterations = 10
  val testsize = 25
  val nanobusy = Seq(0L)
  val nanosleep = {
    val tenthmilli = 100L * 1000L
    val milli = 1000L * 1000L
    Seq(1L, 2L, 3L, 5L).map(_ * tenthmilli) ++
      Seq(1L, 2L, 3L, 5L, 10L).map(_ * milli)
  }

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

//  simpleTestGroup("signal chain",
//    "scalareact" -> (new ReactChainBench(_)),
//    "playground" -> (new DistChainBench(_)),
//    "wrappedplayground" -> (new WrappedChainBench(_, PlaygroundWrapper)),
//    "wrappedscalareact" -> (new WrappedChainBench(_, ScalaReactWrapper())),
//    "wrappedscalarx" -> (new WrappedChainBench(_, ScalaRxWrapper)),
//    "wrappedscalarxparallel" -> (new WrappedChainBench(_, ScalaRxWrapperParallel))
//  )
//
//  simpleTestGroup("signal fan",
//    "scalareact" -> (new ReactFanBench(_)),
//    "playground" -> (new DistFanBench(_)),
//    "wrappedplayground" -> (new WrappedFanBench(_, PlaygroundWrapper)),
//    "wrappedscalareact" -> (new WrappedFanBench(_, ScalaReactWrapper())),
//    "wrappedscalarx" -> (new WrappedFanBench(_, ScalaRxWrapper)),
//    "wrappedscalarxparallel" -> (new WrappedFanBench(_, ScalaRxWrapperParallel))
//  )

  simpleTestGroup("three hosts",
    "wrappedplayground" -> (new ThreeHosts(_, PlaygroundWrapper)),
    "wrappedscalareact" -> (new ThreeHosts(_, ScalaReactWrapper())),
    "wrappedscalarx" -> (new ThreeHosts(_, ScalaRxWrapper)),
    "wrappedscalarxparallel" -> (new ThreeHosts(_, ScalaRxWrapperParallel)),
    "hackkedelmsimulation" -> (new ThreeHosts(_, new ElmSimulationWrapper()))
  )

//  simpleTestGroup("three hosts with independent sources",
//    "wrappedplayground" -> (new IndependentSources(_, PlaygroundWrapper)),
//    "wrappedscalareact" -> (new IndependentSources(_, ScalaReactWrapper())),
//    "wrappedscalarx" -> (new IndependentSources(_, ScalaRxWrapper)),
//    "wrappedscalarxparallel" -> (new IndependentSources(_, ScalaRxWrapperParallel)),
//    "hackkedelmsimulation" -> (new IndependentSources(_, new ElmSimulationWrapper()))
//  )

  simpleTestGroup("three hosts with many sources",
    "wrappedplayground" -> (new ManySources(_, PlaygroundWrapper)),
    "wrappedscalareact" -> (new ManySources(_, ScalaReactWrapper())),
    "wrappedscalarx" -> (new ThreeHosts(_, ScalaRxWrapper)),
    "wrappedscalarxparallel" -> (new ThreeHosts(_, ScalaRxWrapperParallel)),
    "hackkedelmsimulation" -> (new ThreeHosts(_, new ElmSimulationWrapper()))
  )

//  simpleTestGroup("three hosts with many changing sources",
//    "wrappedplayground" -> (new ManyChangingSources(_, PlaygroundWrapper)),
//    "wrappedscalareact" -> (new ManyChangingSources(_, ScalaReactWrapper())),
//    "wrappedscalarx" -> (new ThreeHosts(_, ScalaRxWrapper)),
//    "wrappedscalarxparallel" -> (new ThreeHosts(_, ScalaRxWrapperParallel)),
//    "hackkedelmsimulation" -> (new ThreeHosts(_, new ElmSimulationWrapper()))
//  )

  def simpleTestGroup(groupname: String, tests: Pair[String, Int => SimpleTest]*) =
    performance.of(groupname.replace(' ','_')).config(
      exec.benchRuns -> repetitions,
      exec.maxWarmupRuns -> 4
    ).in {
      tests.foreach { case (name, test) =>
        measure.method(name).in {
          var simpleTest: SimpleTest = null
          using(parameters).beforeTests {
            println(s"before test $groupname $name")
            simpleTest = test(testsize)
            simpleTest.init()
          }.setUp { case (repetitions, iterations, testsize, busytime, sleeptime) =>
            globalUtils.Simulate.nanobusy = busytime
            globalUtils.Simulate.nanosleep = sleeptime
            globalUtils.Simulate.coordinatorsleep = sleeptime
            // manual warmup step …
            simpleTest.run(-42)
            iterate(iterations) { i =>
              val res = simpleTest.run(i)
              assert(simpleTest.validateResult(i, res))
            }
            simpleTest.run(-84)
          }.in { case (_, iterations, _, _, _) =>
            iterate(iterations) { i =>
              val res = simpleTest.run(i)
              assert(simpleTest.validateResult(i, res))
            }
          }
        }
      }
    }

}

