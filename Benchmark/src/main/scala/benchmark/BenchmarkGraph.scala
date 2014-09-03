package benchmark

import scala.language.higherKinds
import benchmark._

sealed trait Source
case object SourceA extends Source
case object SourceB extends Source
case object SourceC extends Source

trait BenchmarkGraphTrait {
  val size: Int

  def set(values: (Source, Int)*): Unit
  def get(source: Source): Int
  def getLast(): Int

  def validateResult: Boolean = theoreticalResult == getLast
  def theoreticalResult: Int
  def state: String
}

class BenchmarkGraph[GenSig[Int], GenVar[Int] <: GenSig[Int]](val wrapper: ReactiveWrapper[GenSig, GenVar], val size: Int = 25) extends BenchmarkGraphTrait {

  import wrapper._

  val source = Map[Source, GenVar[Int]](SourceA -> makeVar(-10), SourceB -> makeVar(-10), SourceC -> makeVar(-10))

  def set(values: (Source, Int)*) = {
    setValues(values.map { case (sourceId, value) => source(sourceId) -> value }: _*)
  }
  def get(sourceId: Source) = getValue(source(sourceId))
  def getLast = getValue(last)

  val secondA = StructureBuilder.makeChain(size, wrapper,
    map(source(SourceA)) { v: Int =>
      v + 1000
    })
  def theoreticalA = (get(SourceA) + 1000 + size)

  val secondB = StructureBuilder.makeFan(size, wrapper,
    map(source(SourceB)) { v: Int =>
      v + 1000
    })
    def theoreticalB = ((get(SourceB) + 1000 + 1) * size)

  val secondC = StructureBuilder.makeRegular(wrapper,
    map(source(SourceC)) { v: Int =>
      v + 1000
    })
    def theoreticalC = (get(SourceC) + 1000 + 9)

  val last = combine(Seq(secondA, secondB, secondC))(_.sum)

  def theoreticalResult: Int = theoreticalA + theoreticalB  + theoreticalC 
  def state = "Sources: A -> %d => %d (expected %d), B -> %d => %d (expected %d), C -> %d => %d (expected %d), Out -> %d (expected: %d)".format(
      get(SourceA), getValue(secondA), theoreticalA,
      get(SourceB), getValue(secondB), theoreticalB,
      get(SourceC), getValue(secondC), theoreticalC,
      getLast, theoreticalResult)
}