package benchmark

import scala.language.higherKinds

class ThreeHosts[GenSig[Int], GenVar[Int] <: GenSig[Int]](length: Int, val wrapper: ReactiveWrapper[GenSig, GenVar]) extends SimpleWaitingTest[GenSig, GenVar] {

  import wrapper._

  val first = makeVar(-10)
  val secondA = StructureBuilder.makeChain(length, wrapper, {
    map(first){v: Int =>
      Simulate.network()
      v + 1000
    }
  })

  val secondB = StructureBuilder.makeFan(length, wrapper, {
    map(first){v: Int =>
      Simulate.network()
      v + 1000
    }
  })

  val secondC = map(first){v: Int =>
      Simulate.network()
      v + 1000
    }

  val last = map(transpose(Seq(secondA, secondB, secondC)))(vs => {Simulate.network(); vs.sum})


}