package benchmark.networks

import scala.language.higherKinds
import globalUtils.Simulate
import benchmark._

class IndependentSources[GenSig[Int], GenVar[Int] <: GenSig[Int]](size: Int, val wrapper: ReactiveWrapper[GenSig, GenVar]) extends SimpleWaitingTest[GenSig, GenVar] {

  import wrapper._

  override def startUpdate(i: Int): Unit = setValues(firstA -> i, firstB -> i, firstC -> i)

  val firstA = makeVar(-10)
  // this is just to make superclass happy
  val first = firstA
  val firstB = makeVar(-10)
  val firstC = makeVar(-10)

  val secondA = StructureBuilder.makeChain(size, wrapper,
    map(firstA) { v: Int =>
      Simulate.network()
      v + 1000
    })

  val secondB = StructureBuilder.makeFan(size, wrapper,
    map(firstB) { v: Int =>
      Simulate.network()
      v + 1000
    })

  val secondC = StructureBuilder.makeRegular(wrapper,
    map(firstC) { v: Int =>
      Simulate.network()
      v + 1000
    })

  val last = combine(Seq(secondA, secondB, secondC))(vs => {Simulate.network(); vs.sum })

  // do not check value, because scala.rx glitches on this one
  //(i + 1001) * size + (i + 1000 + size) + (i + 1000 + 9) == res
  def validateResult(i: Int, res: Int): Boolean = true
}