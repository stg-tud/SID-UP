package benchmark.networks

import scala.language.higherKinds
import globalUtils.Simulate
import benchmark.{StructureBuilder, SimpleWaitingTest, ReactiveWrapper}

class ManySources[GenSig[Int], GenVar[Int] <: GenSig[Int]](size: Int, val wrapper: ReactiveWrapper[GenSig, GenVar]) extends SimpleWaitingTest[GenSig, GenVar] {

  import wrapper._

  def sourceSize = size

  def additionalSources = 2

  val sourceList = Range(0, sourceSize).map(_ => makeVar(0))

  val first = sourceList.head
  val transposed = transpose(sourceList)

  val secondA = StructureBuilder.makeChain(size, wrapper, {
    map(transposed) { v: Seq[Int] =>
      Simulate.network()
      v.sum + 1000
    }
  }, additionalSources)

  val secondB = StructureBuilder.makeFan(size, wrapper, {
    map(transposed) { v: Seq[Int] =>
      Simulate.network()
      v.sum + 1000
    }
  }, additionalSources)

  val secondC = StructureBuilder.makeRegular(wrapper,
    map(transposed) { v: Seq[Int] =>
      Simulate.network()
      v.sum + 1000
    })

  val last = combine(Seq(secondA, secondB, secondC))(vs => {Simulate.network(); vs.sum })

  def validateResult(i: Int, res: Int): Boolean =
    (i + 1001) * size + (i + 1000 + size) + (i + 1000 + 9) == res
}