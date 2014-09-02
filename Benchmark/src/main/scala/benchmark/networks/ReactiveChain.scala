package benchmark.networks

import benchmark._
import scala.language.higherKinds
import scala.react.Domain
import globalUtils.Simulate
import benchmark.{StructureBuilder, SimpleWaitingTest, ReactiveWrapper}

class WrappedChainBench[GenSig[Int], GenVar[Int] <: GenSig[Int]](length: Int, val wrapper: ReactiveWrapper[GenSig, GenVar]) extends SimpleWaitingTest[GenSig, GenVar] {

  import wrapper._

  val first = makeVar(-1)
  val last = StructureBuilder.makeChain(length, wrapper, first)

  def validateResult(i: Int, res: Int): Boolean = i + length == res
}

class ReactChainBench(length: Int) extends Domain with SimpleTest {
  val scheduler = new ManualScheduler()
  val engine = new Engine()

  def run(i: Int) = {
    schedule(first() = i)
    runTurn(())
    last.getValue
  }

  def validateResult(i: Int, res: Int): Boolean = i + length == res

  val first = Var(-1)
  val last = {
    var curr: Signal[Int] = first
    Range(0, length).foreach {
      i =>
        val last = curr
        schedule {
          curr = Strict {
            Simulate()
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

  def validateResult(i: Int, res: Int): Boolean = i + length == res

  val first = Var(-1)
  val last = {
    var curr: Signal[Int] = first
    Range(0, length).foreach {
      i =>
        curr = curr.map {
          v =>
            Simulate()
            v + 1
        }
    }
    curr
  }
}