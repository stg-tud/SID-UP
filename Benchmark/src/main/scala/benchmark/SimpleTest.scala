package benchmark

import scala.language.higherKinds

trait SimpleTest {
  def run(i: Int): Int
  def init(): Any = ()
  def validateResult(i: Int, res: Int): Boolean
}

trait SimpleWaitingTest[GenSig[Int], GenVar[Int] <: GenSig[Int]] extends SimpleTest {

  def wrapper: ReactiveWrapper[GenSig, GenVar]

  def run(i: Int): Int = {
    val await = wrapper.awaiter(last)
    startUpdate(i)
    await()
    wrapper.getValue(last)
  }

  def startUpdate(i: Int): Unit = wrapper.setValue(first)(i)

  override def init() = {}

  def first: GenVar[Int]

  def last: GenSig[Int]
}
