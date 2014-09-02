package benchmark.networks

import scala.language.higherKinds
import benchmark._

class WrappedFanBench[GenSig[Int], GenVar[Int] <: GenSig[Int]](width: Int, val wrapper: ReactiveWrapper[GenSig, GenVar]) extends SimpleWaitingTest[GenSig, GenVar] {

  import wrapper._

  val first = makeVar(-1)
  val last = StructureBuilder.makeFan(width, wrapper, first)

  def validateResult(i: Int, res: Int): Boolean = (i + 1) * width == res
}

class DistFanBench(width: Int) extends SimpleTest {

  import reactive.signals._

  def run(i: Int) = {
    first << i
    last.now
  }

  def validateResult(i: Int, res: Int): Boolean = (i + 1) * width == res

  val first = Var(-1)
  val last = {
    val fanned = Range(0, width).map {
      i =>
        first.map { v =>
          v + 1
        }
    }
    new impl.FunctionalSignal({
      t => fanned.map(_.now).sum
    }, fanned: _*)
  }
}

