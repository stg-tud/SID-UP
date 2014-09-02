package benchmark.networks

import scala.language.higherKinds
import globalUtils.Simulate
import benchmark._

class ThreeHosts[GenSig[Int], GenVar[Int] <: GenSig[Int]](size: Int, val wrapper: ReactiveWrapper[GenSig, GenVar]) extends SimpleWaitingTest[GenSig, GenVar] {

  import wrapper._

  val first = makeVar(-10)
  val secondA = StructureBuilder.makeChain(size, wrapper, {
    map(first) { v: Int =>
      Simulate.network()
      v + 1000
    }
  })

  val secondB = StructureBuilder.makeFan(size, wrapper, {
    map(first) { v: Int =>
      Simulate.network()
      v + 1000
    }
  })

  val secondC = StructureBuilder.makeRegular(wrapper,
    map(first) { v: Int =>
      Simulate.network()
      v + 1000
    })

  val last = combine(Seq(secondA, secondB, secondC))(vs => {Simulate.network(); vs.sum })

  def validateResult(i: Int, res: Int): Boolean =
    (i + 1001) * size + (i + 1000 + size) + (i + 1000 + 9) == res
}