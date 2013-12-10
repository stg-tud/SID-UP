package benchmark

import scala.language.higherKinds
import globalUtils.Simulate

object StructureBuilder {
  def makeChain[GenSig[Int], GenVar[Int] <: GenSig[Int]](length: Int, wrapper: ReactiveWrapper[GenSig, GenVar], start: GenSig[Int], additionalSources: Int = 0): GenSig[Int] = {
    import wrapper._
    var curr: GenSig[Int] = start
    Range(0, length).foreach { i =>
      val sources = curr +: Range(0, additionalSources).map(makeVar(_))
      curr = combine(sources) { vs =>
        Simulate()
        vs(0) + 1
      }
    }
    curr
  }

  def makeFan[GenSig[Int], GenVar[Int] <: GenSig[Int]](width: Int, wrapper: ReactiveWrapper[GenSig, GenVar], start: GenSig[Int], additionalSources: Int = 0): GenSig[Int] = {
    import wrapper._
    val fanned = Range(0, width).map { i =>
      val sources = start +: Range(0, additionalSources).map(makeVar(_))
      combine(sources) { vs =>
        Simulate()
        vs(0) + 1
      }
    }
    combine(fanned)(_.sum)
  }
}
