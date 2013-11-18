package benchmark

import scala.language.higherKinds
import scala.react.Domain


class WrappedFanBench[GenSig[Int], GenVar[Int] <: GenSig[Int]](width: Int, wrapper: ReactiveWrapper[GenSig, GenVar]) extends SimpleTest {

  import wrapper._

  def run(i: Int): Int = {
    setValue(first)(i)
    getValue(last)
  }

  val first = makeVar(-1)
  val last = StructureBuilder.makeFan(width, wrapper, first)
}

class DistFanBench(width: Int) extends SimpleTest {

  import reactive.signals._

  def run(i: Int) = {
    first << i
    last.now
  }

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
