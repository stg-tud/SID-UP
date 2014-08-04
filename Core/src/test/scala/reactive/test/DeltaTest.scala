package reactive.test

import org.scalatest.FunSuite
import reactive.signals.Var

class DeltaTest extends FunSuite {
  test("delta works") {
    val in = Var(3)
    val out = in.single.delta
    val log = out.single.log

    in << 1
    in << 5
    in << 5
    in << 10

    Thread.sleep(10)
    assertResult(List(3 -> 1, 1 -> 5, 5 -> 10)) { log.single.now }
  }
}
