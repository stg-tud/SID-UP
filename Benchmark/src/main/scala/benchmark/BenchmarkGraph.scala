package benchmark

import scala.language.higherKinds
import benchmark._

sealed trait Source
case object Chain extends Source
case object Fan extends Source
case object Regular extends Source

trait BenchmarkGraphTrait {
  val size: Int

  def set(values: (Source, Int)*): Unit
  def get(source: Source): Int
  def getLast(): Int

  def validateResult: Boolean = theoreticalResult == getLast
  def theoreticalResult: Int
  def state: String
  def reset(): Unit
}

case class SingleSourceBenchmarkGraph[GenSig[Int], GenVar[Int] <: GenSig[Int]](val wrapper: ReactiveWrapper[GenSig, GenVar], val size: Int = 25) extends BenchmarkGraphTrait {
  val initialValue = -10

  import wrapper._

  val source = Map[Source, GenVar[Int]](Chain -> makeVar(0), Fan -> makeVar(0), Regular -> makeVar(0))

  def reset = {
    set(Chain -> initialValue, Fan -> initialValue, Regular -> initialValue)
    assert(validateResult)
  }

  def set(values: (Source, Int)*) = {
    setValues(values.map { case (sourceId, value) => source(sourceId) -> value }: _*)
  }
  def get(sourceId: Source) = getValue(source(sourceId))
  def getLast = getValue(last)

  val secondA = StructureBuilder.makeChain(size, wrapper,
    map(source(Chain)) { v: Int =>
      v + 1000
    })
  def theoreticalA = (get(Chain) + 1000 + size)

  val secondB = StructureBuilder.makeFan(size, wrapper,
    map(source(Fan)) { v: Int =>
      v + 1000
    })
  def theoreticalB = ((get(Fan) + 1000 + 1) * size)

  val secondC = StructureBuilder.makeRegular(wrapper,
    map(source(Regular)) { v: Int =>
      v + 1000
    })
  def theoreticalC = (get(Regular) + 1000 + 9)

  val last = combine(Seq(secondA, secondB, secondC))(_.sum)

  def theoreticalResult: Int = theoreticalA + theoreticalB + theoreticalC
  def state = "Sources: A -> %d => %d (expected %d), B -> %d => %d (expected %d), C -> %d => %d (expected %d), Out -> %d (expected: %d)".format(
    get(Chain), getValue(secondA), theoreticalA,
    get(Fan), getValue(secondB), theoreticalB,
    get(Regular), getValue(secondC), theoreticalC,
    getLast, theoreticalResult)

  reset
}

case class ManySourcesBenchmarkGraph[GenSig[Int], GenVar[Int] <: GenSig[Int]](val wrapper: ReactiveWrapper[GenSig, GenVar], val size: Int = 25, val sourceCount: Int = 20, val sourceUpdateCount: Int = 4) extends BenchmarkGraphTrait {
  val initialValue = -10

  import wrapper._

  val sources = Map[Source, Seq[GenVar[Int]]](Chain -> (1 to sourceCount).map { _ => makeVar(0) }.toSeq, Fan -> (0 to sourceCount).map { _ => makeVar(0) }.toSeq, Regular -> (0 to sourceCount).map { _ => makeVar(0) }.toSeq)

  def reset = {
    set(Chain -> initialValue, Fan -> initialValue, Regular -> initialValue)
    assert(validateResult, state)
  }

  def set(values: (Source, Int)*) = {
    setValues(values.flatMap { case (sourceId, value) => sources(sourceId).take(sourceUpdateCount).map { _ -> value } }: _*)
  }
  def get(sourceId: Source) = getValue(sources(sourceId).head)
  def getLast = getValue(last)

  val secondA = StructureBuilder.makeChain(size, wrapper,
    combine(sources(Chain)) { _.sum + 1000 })
  def theoreticalA = (sourceUpdateCount * get(Chain) + 1000 + size)

  val secondB = StructureBuilder.makeFan(size, wrapper,
    combine(sources(Fan)) { _.sum + 1000 })
  def theoreticalB = ((sourceUpdateCount * get(Fan) + 1000 + 1) * size)

  val secondC = StructureBuilder.makeRegular(wrapper,
    combine(sources(Regular)) { _.sum + 1000 })
  def theoreticalC = (sourceUpdateCount * get(Regular) + 1000 + 9)

  val last = combine(Seq(secondA, secondB, secondC))(_.sum)

  def theoreticalResult: Int = theoreticalA + theoreticalB + theoreticalC
  def state = "Sources: A -> %d => %d (expected %d), B -> %d => %d (expected %d), C -> %d => %d (expected %d), Out -> %d (expected: %d)".format(
    get(Chain), getValue(secondA), theoreticalA,
    get(Fan), getValue(secondB), theoreticalB,
    get(Regular), getValue(secondC), theoreticalC,
    getLast, theoreticalResult)

  reset
}