package unoptimized.test

import org.scalatest.FunSuite
import unoptimized.signals.Var

class DeltaTest extends FunSuite {
  test("delta works") {
    val in = Var(3)
    val out = in.delta
    val log = out.log

    in << 1
    in << 5
    in << 5
    in << 10

    Thread.sleep(10);
    assertResult(List(3 -> 1, 1 -> 5, 5 -> 10)) { log.now }
  }
}
