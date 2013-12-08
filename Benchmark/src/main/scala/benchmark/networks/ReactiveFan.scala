package benchmark.networks

import scala.language.higherKinds
import scala.react.Domain
import globalUtils.Simulate
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
          Simulate()
          v + 1
        }
    }
    new impl.FunctionalSignal({
      t => fanned.map(_.now).sum
    }, fanned: _*)
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

  def validateResult(i: Int, res: Int): Boolean = (i + 1) * width == res

  val first = Var(-1)
  val last = {
    var fanned = Seq[Signal[Int]]()
    schedule {
      fanned = Range(0, width).map { i =>
        Strict {
          Simulate()
          1 + first()
        }
      }
    }
    var last: Option[Signal[Int]] = None
    schedule {
      last = Some(Strict {
        fanned.map { _() }.sum
      })
    }
    runTurn(())
    last.get
  }
}
