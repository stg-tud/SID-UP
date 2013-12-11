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

  def makeRegular[GenSig[Int], GenVar[Int] <: GenSig[Int]](wrapper: ReactiveWrapper[GenSig, GenVar], start: GenSig[Int]): GenSig[Int] = {
    import wrapper._

    def inc(source: GenSig[Int]): GenSig[Int] = map(source){v => {Simulate(); v + 1}}
    def sum(sources: GenSig[Int]*): GenSig[Int] = combine(sources){vs => {Simulate(); vs.sum}}
    def noc(sources: GenSig[Int]*): GenSig[Int] = combine(sources){_ => {Simulate(); 0}}

    // row 3
    val c1 = inc(start)

    // row 2
    val b1 = inc(c1)
    val b2 = inc(b1)
    val b3 = inc(b2)

    // row 3
    val c2 = inc(b3)
    val c3 = noc(c2)
    val c4 = inc(c3)

    // row 1
    val a1 = inc(b2)
    val a2 = inc(a1)
    val a3 = sum(a2, b2)
    val a4 = inc(a3)

    // row2
    val b4 = noc(a4, b3)
    val b5 = inc(b4)
    val b6 = inc(b5)
    val b7 = inc(b6)
    val b8 = sum(b7, c2)

    // row 3
    val c5  = sum(c4, b8)

    // row 4
    val d1 = inc(c2)

    // row 5
    val e1 = noc(c1)
    val e2 = inc(e1)
    val e3 = inc(e2)
    val e4 = inc(e3)
    val e5 = sum(e4, c2)

    val e6 = inc(c2)
    val e7 = sum(e6, d1)

    c5
  }
}
