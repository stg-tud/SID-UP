package benchmark

import scala.language.higherKinds
import globalUtils.Simulate

object StructureBuilder {
  def makeChain[GenSig[Int], GenVar[Int] <: GenSig[Int]](length: Int, wrapper: ReactiveWrapper[GenSig, GenVar], start: GenSig[Int]): GenSig[Int] = {
    import wrapper._
    var curr: GenSig[Int] = start
    Range(0, length).foreach {
      i =>
        curr = map(curr) {
          v =>
            Simulate()
            v + 1
        }
    }
    curr
  }

  def makeFan[GenSig[Int], GenVar[Int] <: GenSig[Int]](width: Int, wrapper: ReactiveWrapper[GenSig, GenVar], start: GenSig[Int]): GenSig[Int] = {
    import wrapper._
    val fanned = Range(0, width).map { i =>
      map(start) { v =>
        Simulate()
        v + 1
      }
    }
    map(transpose(fanned))(_.sum)
  }
}
