package unoptimized.test
import unoptimized.events.EventSource
import unoptimized.TransactionBuilder
import org.scalatest.FunSuite
import unoptimized.signals.Var

class CollectTest extends FunSuite {
  test("collect event stream works") {
    val e = EventSource[Int]

    val isEven: PartialFunction[Int, String] = {
      case x if x % 2 == 0 => x + " is even"
    }

    val even = e.collect(isEven)

    assertResult(List("2 is even", "4 is even", "6 is even", "8 is even", "10 is even")) {
      val result = even.log
      for (sample <- 1 to 10) e << sample
      result.now
    }
  }
}
