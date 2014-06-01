package reactive.test

import org.scalatest.{ Tag, FunSuite }
import reactive.signals.Var
import reactive.signals.Val
import reactive.signals.RoutableVar
import reactive.TransactionBuilder
import reactive.Lift._
import scala.concurrent.stm._
import reactive.signals.Signal

class SignalMonadicTest extends FunSuite {
  test("explicit usage: changing inner and outer values") {
    val sx = Var(1)
    val sy = Var(2)
    val ssumxy = atomic { tx => sx.tflatMap { (x, itx) => sy.map { y => x + y }(itx) }(tx) }
    val ssumyx = atomic { tx => sy.tflatMap { (y, itx) => sx.map { x => x + y }(itx) }(tx) }
    assertResult(3)(ssumxy.single.now)
    assertResult(3)(ssumyx.single.now)

    sx << 10
    assertResult(12)(ssumxy.single.now)
    assertResult(12)(ssumyx.single.now)

    sy << 10

    assertResult(20)(ssumxy.single.now)
    assertResult(20)(ssumyx.single.now)

    sx << 6
    sy << 7

    assertResult(13)(ssumxy.single.now)
    assertResult(13)(ssumyx.single.now)
  }

  test("explicit usage: observing flattened values") {
    val sx = Var(1)
    val sy = Var(2)

    val ssum = atomic { tx =>
      sx.tflatMap { (x, itx) => sy.map { y => x + y }(itx) }(tx)
    }

    ssum.single.observe { sum => assertResult(sum)(sx.single.now + sy.single.now) }

    sx << 5
    sy << 5
  }

  test("for syntax: changing inner and outer values") {
    val sx = Var(1)
    val sy = Var(2)
    val ssumxy = atomic { implicit tx => for (x <- sx; y <- sy) yield x + y }
    val ssumyx = atomic { implicit tx => for (y <- sy; x <- sx) yield x + y }
    assertResult(3)(ssumxy.single.now)
    assertResult(3)(ssumyx.single.now)

    sx << 10
    assertResult(12)(ssumxy.single.now)
    assertResult(12)(ssumyx.single.now)

    sy << 10

    assertResult(20)(ssumxy.single.now)
    assertResult(20)(ssumyx.single.now)

    sx << 6
    sy << 7

    assertResult(13)(ssumxy.single.now)
    assertResult(13)(ssumyx.single.now)
  }

  test("combining flattened values") {
    val sx = Var(1)
    val sy = Var(2)

    val ssum = atomic { implicit tx => for (x <- sx; y <- sy) yield x + y }
    val sdiff = atomic { implicit tx => for (y <- sy; x <- sx) yield x - y }

    val ssumdiff = atomic { implicit tx => for (sum <- ssum; diff <- sdiff) yield (sum, diff) }

    assertResult((3, -1))(ssumdiff.single.now)

    sx << 10

    assertResult((12, 8))(ssumdiff.single.now)

    //sy << 10 //TODO: this will fail with a timeout, see transactions below
  }

  test("observe combined values") {
    val sx = Var(1)
    val sy = Var(2)

    val ssum = atomic { implicit tx => for (x <- sx; y <- sy) yield x + y }
    val sdiff = atomic { implicit tx => for (y <- sy; x <- sx) yield x - y }

    val ssumdiff = atomic { implicit tx => for (sum <- ssum; diff <- sdiff) yield (sum, diff) }

    ssumdiff.single.observe {
      case (sum, diff) =>
        assertResult(sum)(ssum.single.now)
        assertResult(diff)(sdiff.single.now)
    }

    sx << 9
    //sy << 100 //TODO: this will fail with a timeout, see transactions below
  }

  test("combining more signals") {
    val s1 = Var(1)
    val s2 = Var(2)
    val s3 = Var(3)
    val s4 = Var(4)
    val sall = atomic { implicit tx => for (v1 <- s1; v2 <- s2; v3 <- s3; v4 <- s4) yield (v1, v2, v3, v4) }

    assertResult((1, 2, 3, 4))(sall.single.now)

    s1 << 5
    s2 << 6
    s3 << 7
    s4 << 8

    assertResult((5, 6, 7, 8))(sall.single.now)
  }

  // failures of this test are nondeterministic some orders of the transaction
  // execution will timeout, others will not. this is probably caused by s1
  // updated before s2 which will recompute the inner signal, which then loses
  // track of the commit count. or something along those lines :)
  test("together with transactions") {
    val s1 = Var(1)
    val s2 = Var(2)
    val sall = atomic { implicit tx => for (v1 <- s1; v2 <- s2) yield (v1, v2) }

    val allLog = sall.single.log

    assertResult(List((1, 2)))(allLog.single.now)

    val transaction = new TransactionBuilder()
    transaction.set(s1, 5)
    transaction.set(s2, 5)
    transaction.commit()

    assertResult(List((1, 2), (5, 5)))(allLog.single.now)
  }

  test("branch instead of transaction") {
    val s = Var(1)
    val s2 = s.single.map(_ - 3)
    val s1 = s.single.map(_ + 3)

    val sall = atomic { implicit tx => for (v1 <- s1; v2 <- s2) yield (v1, v2) }

    val allLog = sall.single.log

    assertResult(List((4, -2)))(allLog.single.now)

    s << 5

    assertResult(List((4, -2), (8, 2)))(allLog.single.now)
  }

  test("Val and Routable") {
    val s1 = new Val(1)
    val s2 = new Val(2)
    val rs1 = RoutableVar(s1)
    val rs2 = RoutableVar(s2)

    val combined1 = atomic { implicit tx => for (v1 <- rs1; v2 <- s2) yield (v1, v2) }
    val combined2 = atomic { implicit tx => for (v1 <- s1; v2 <- rs2) yield (v1, v2) }

    assertResult((1, 2))(combined1.single.now)
    assertResult((1, 2))(combined2.single.now)

    rs2 << new Val(9)
    assertResult((1, 9))(combined2.single.now)

    val s3 = Var(3)
    rs1 << s3
    assertResult((3, 2))(combined1.single.now)

    s3 << 5
    assertResult((5, 2))(combined1.single.now)
  }
}
