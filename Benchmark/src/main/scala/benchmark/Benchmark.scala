package benchmark

import org.scalameter.api._
import scala.react.Domain
import reactive.signals.impl.FunctionalSignal

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

  val persistor = new SerializationPersistor("./results/")

  val repetitions = 50
  val iterations = 10
  val testSize = 100
  val nanosleep = 1000

  def iterate[T](iterations: Int)(f: Int => T) = {
    var i = 0
    while (i < iterations - 1) {
      f(i)
      i += 1
    }
  }

  var simpleTest: SimpleTest = _

  performance.of("signal chain").config(
    exec.benchRuns -> repetitions
  ).in {
    measure.method("scala.react").in {
      using(Gen.unit("none")).beforeTests {
        simpleTest = new ReactChainBench(testSize)
      }.in { _ =>
        iterate(iterations) { i =>
          assert(i + testSize == simpleTest.run(i))
        }
      }
    }

    measure.method("playground").in {
      using(Gen.unit("none")).beforeTests {
        simpleTest = new DistChainBench(testSize)
      }.in { _ =>
        iterate(iterations) { i =>
          assert(i + testSize == simpleTest.run(i))
        }
      }
    }
  }

  performance.of("signal fan").config(
    exec.benchRuns -> repetitions
  ).in {
    measure.method("scala.react").in {
      using(Gen.unit("none")).beforeTests {
        simpleTest = new ReactFanBench(testSize)
      }.in { _ =>
        iterate(iterations) { i =>
          assert((i + 1) * testSize == simpleTest.run(i))
        }
      }
    }

    measure.method("playground").in {
      using(Gen.unit("none")).beforeTests {
        simpleTest = new DistFanBench(testSize)
      }.in { _ =>
        iterate(iterations) { i =>
          assert((i + 1) * testSize == simpleTest.run(i))
        }
      }
    }
  }

}

object simulateWork {
  def apply() = {
    val nanos = DistReactBenchmark.nanosleep
    if (nanos > 0) {
      val ct = System.nanoTime()
      while (ct + nanos > System.nanoTime()) {}
    }
  }
}

trait SimpleTest {
  def run(i: Int): Int
}

class DistChainBench(length: Int) extends SimpleTest {

  import reactive.signals._

  def run(i: Int) = {
    first << i
    last.now
  }

  val first = Var(-1)
  val last = {
    var curr: Signal[Int] = first
    Range(0, length).foreach { i =>
      curr = curr.map { v =>
        simulateWork()
        v + 1
      }
    }
    curr
  }
}

class ReactChainBench(length: Int) extends Domain with SimpleTest {
  val scheduler = new ManualScheduler()
  val engine = new Engine()

  def run(i: Int) = {
    schedule(first() = i)
    runTurn(())
    last.getValue
  }

  val first = Var(-1)
  val last = {
    var curr: Signal[Int] = first
    Range(0, length).foreach { i =>
      val last = curr
      schedule {
        curr = Strict {
          simulateWork()
          last() + 1
        }
      }
      runTurn(())
    }
    curr
  }
}

class DistFanBench(width: Int) extends SimpleTest {

  import reactive.signals._

  def run(i: Int) = {
    first << i
    last.now
  }

  val first = Var(-1)
  val last = {
    val fanned = Range(0, width).map { i =>
      first.map {
        simulateWork()
        _ + 1
      }
    }
    new FunctionalSignal({ t => fanned.map(_.now).sum}, fanned: _*)
  }
}

class ReactFanBench(width: Int) extends Domain with SimpleTest {
  val scheduler = new ManualScheduler()
  val engine = new Engine()

  def run(i: Int) = {
    schedule(first() = i)
    runTurn(())
    last.getValue
  }

  val first = Var(-1)
  val last = {
    var fanned = Seq[Signal[Int]]()
    schedule {
      fanned = Range(0, width).map { i =>
        Strict {
          simulateWork()
          1 + first()
        }
      }
    }
    var last: Option[Signal[Int]] = None
    schedule {
      last = Some(Strict {fanned.map {_()}.sum})
    }
    runTurn(())
    last.get
  }
}
