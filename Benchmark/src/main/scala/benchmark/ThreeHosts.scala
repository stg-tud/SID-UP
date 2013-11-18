package benchmark

import scala.language.higherKinds
import scala.react.Domain
import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration.{DurationLong, Duration}
import java.util.concurrent.TimeoutException

class ThreeHosts[GenSig[Int], GenVar[Int] <: GenSig[Int]](length: Int, wrapper: ReactiveWrapper[GenSig, GenVar]) extends SimpleTest {

  import wrapper._

  def run(i: Int): Int = {
    val done = Promise[Int]()
    runDone = v => { done.success(v)}
    setValue(first)(i)
    Await.ready(done.future,Duration.Inf)
    getValue(last)
  }

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

  var runDone: Int => Unit = v => ()
  val observer = observe(last)(v => runDone(v))


}