package benchmark

import scala.react.Domain

class WrappedChainBench[GenSig[Int], GenVar[Int] <: GenSig[Int]](length: Int, wrapper: ReactiveWrapper[GenSig, GenVar]) extends SimpleTest {

  import wrapper._

  def run(i: Int): Int = {
    setValue(first)(i)
    getValue(last)
  }

  val first = makeVar(-1)
  val last = {
    var curr: GenSig[Int] = first
    Range(0, length).foreach {
      i =>
        curr = map(curr) {
          v =>
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
    Range(0, length).foreach {
      i =>
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

class DistChainBench(length: Int) extends SimpleTest {

  import reactive.signals._

  def run(i: Int) = {
    first << i
    last.now
  }

  val first = Var(-1)
  val last = {
    var curr: Signal[Int] = first
    Range(0, length).foreach {
      i =>
        curr = curr.map {
          v =>
            simulateWork()
            v + 1
        }
    }
    curr
  }
}