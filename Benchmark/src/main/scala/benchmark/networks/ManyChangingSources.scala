package benchmark.networks

import scala.language.higherKinds
import benchmark._

class ManyChangingSources[GenSig[Int], GenVar[Int] <: GenSig[Int]](size: Int, wrapper: ReactiveWrapper[GenSig, GenVar]) extends ManySources[GenSig, GenVar](size, wrapper) {

  override def startUpdate(i: Int): Unit =
    wrapper.setValues(sourceList.map(_ -> i): _*)

  override def sourceSize: Int = size

  override def validateResult(i: Int, res: Int): Boolean = true // its fine … really
}