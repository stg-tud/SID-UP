package benchmark

import scala.language.higherKinds

class ManySources[GenSig[Int], GenVar[Int] <: GenSig[Int]](size: Int, val wrapper: ReactiveWrapper[GenSig, GenVar]) extends SimpleWaitingTest[GenSig, GenVar] {

  import wrapper._

  def sourceSize = size * 1000

  val sourceList = Range(0, sourceSize).map(_ => makeVar(0))

  val sourceListA = transpose(Range(0, sourceSize).map(_ => makeVar(0)))
  val sourceListB = transpose(Range(0, sourceSize).map(_ => makeVar(0)))
  val sourceListC = transpose(Range(0, sourceSize).map(_ => makeVar(0)))
  
  val first = sourceList.head
  val transposed = transpose(sourceList)
  val secondA = StructureBuilder.makeChain(size, wrapper, {
    val netSig = map(transposed){v: Seq[Int] =>
      Simulate.network()
      Seq(v.sum + 1000)
    }
    map(transpose(Seq(netSig,sourceListA))){_.head.head}
  })

  val secondB = StructureBuilder.makeFan(size, wrapper, {
    val netSig = map(transposed){v: Seq[Int] =>
      Simulate.network()
      Seq(v.sum + 1000)
    }
    map(transpose(Seq(netSig,sourceListA))){_.head.head}
  })

  val secondC = map(transposed){v: Seq[Int] =>
    Simulate.network()
    v.sum + 1000
  }

  val last = map(transpose(Seq(secondA, secondB, secondC)))(vs => {Simulate.network(); vs.sum})

  def validateResult(i: Int, res: Int): Boolean =
    (i + 1001) * size + (i + 1000 + size) + ( i + 1000) == res
}