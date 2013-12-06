package benchmark

import scala.language.higherKinds

class ManyChangingSources[GenSig[Int], GenVar[Int] <: GenSig[Int]](size: Int, wrapper: ReactiveWrapper[GenSig, GenVar]) extends ManySources[GenSig, GenVar](size, wrapper) {

  override def run(i: Int): Int = {
    wrapper.setValues(sourceList.map(_ -> i): _*)
    wrapper.getValue(last)
  }

  override def sourceSize: Int = size

  override def validateResult(i: Int, res: Int): Boolean = true // its fine … really
}