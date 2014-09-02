package benchmark.networks

import benchmark._
import scala.language.higherKinds
import benchmark.{StructureBuilder, SimpleWaitingTest, ReactiveWrapper}

class WrappedChainBench[GenSig[Int], GenVar[Int] <: GenSig[Int]](length: Int, val wrapper: ReactiveWrapper[GenSig, GenVar]) extends SimpleWaitingTest[GenSig, GenVar] {

  import wrapper._

  val first = makeVar(-1)
  val last = StructureBuilder.makeChain(length, wrapper, first)

  def validateResult(i: Int, res: Int): Boolean = i + length == res
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
        curr = curr.map { _ + 1 }
    }
    curr
  }
}