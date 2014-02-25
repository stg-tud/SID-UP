package reactive.test

import org.scalatest.{Tag, FunSuite}
import reactive.signals.Var
import reactive.signals.Val
import reactive.signals.RoutableVar
import reactive.TransactionBuilder
import reactive.Lift._

class SignalMonadicTest extends FunSuite {
  test("explicit usage: changing inner and outer values") {
    val sx = Var(1)
    val sy = Var(2)
    val ssumxy = sx.flatMap{ x => sy.map{ y => x + y}}
    val ssumyx = sy.flatMap{ y => sx.map{ x => x + y}}
    assertResult(3)(ssumxy.now)
    assertResult(3)(ssumyx.now)

    sx << 10
    assertResult(12)(ssumxy.now)
    assertResult(12)(ssumyx.now)

    sy << 10

    assertResult(20)(ssumxy.now)
    assertResult(20)(ssumyx.now)

    sx << 6
    sy << 7

    assertResult(13)(ssumxy.now)
    assertResult(13)(ssumyx.now)
  }

  test("explicit usage: observing flattened values") {
    val sx = Var(1)
    val sy = Var(2)

    val ssum = sx.flatMap{ x => sy.map{ y => x + y } }

    ssum.observe{ sum => assertResult(sum)(sx.now + sy.now) }

    sx << 5
    sy << 5
  }

  test("for syntax: changing inner and outer values") {
    val sx = Var(1)
    val sy = Var(2)
    val ssumxy = for (x <- sx; y <- sy) yield x + y
    val ssumyx = for (y <- sy; x <- sx) yield x + y
    assertResult(3)(ssumxy.now)
    assertResult(3)(ssumyx.now)

    sx << 10
    assertResult(12)(ssumxy.now)
    assertResult(12)(ssumyx.now)

    sy << 10

    assertResult(20)(ssumxy.now)
    assertResult(20)(ssumyx.now)

    sx << 6
    sy << 7

    assertResult(13)(ssumxy.now)
    assertResult(13)(ssumyx.now)
  }


  ignore("combining flattened values") {
    val sx = Var(1)
    val sy = Var(2)

    val ssum = for (x <- sx; y <- sy) yield x + y
    val sdiff = for (y <- sy; x <- sx) yield x - y

    val ssumdiff = for (sum <- ssum; diff <- sdiff) yield (sum, diff)

    assertResult((3, -1))(ssumdiff.now)

    sx << 10

    assertResult((12, 8))(ssumdiff.now)

    //sy << 10 //TODO: this will fail with a timeout, see transactions below
  }

  test("observe combined values") {
    val sx = Var(1)
    val sy = Var(2)

    val ssum = for (x <- sx; y <- sy) yield x + y
    val sdiff = for (y <- sy; x <- sx) yield x - y

    val ssumdiff = for (sum <- ssum; diff <- sdiff) yield (sum, diff)

    ssumdiff.observe{ case (sum, diff) =>
      assertResult(sum)(ssum.now)
      assertResult(diff)(sdiff.now)
    }

    sx << 9
    //sy << 100 //TODO: this will fail with a timeout, see transactions below
  }

  test("combining more signals") {
    val s1 = Var(1)
    val s2 = Var(2)
    val s3 = Var(3)
    val s4 = Var(4)
    val sall = for(v1 <- s1; v2 <- s2; v3 <- s3; v4 <- s4) yield (v1,v2,v3,v4)

    assertResult((1,2,3,4))(sall.now)

    s1 << 5
    s2 << 6
    s3 << 7
    s4 << 8

    assertResult((5,6,7,8))(sall.now)

  }

  // failures of this test are nondeterministic some orders of the transaction
  // execution will timeout, others will not. this is probably caused by s1
  // updated before s2 which will recompute the inner signal, which then loses
  // track of the commit count. or something along those lines :)
  ignore("together with transactions") {
    val s1 = Var(1)
    val s2 = Var(2)
    val sall = for(v1 <- s1; v2 <- s2) yield (v1,v2)

    val allLog = sall.log

    assertResult(List((1,2)))(allLog.now)

    val transaction = new TransactionBuilder()
    transaction.set(s1, 5)
    transaction.set(s2, 5)
    transaction.commit()

    assertResult(List((1,2),(5,5)))(allLog.now)
  }

  ignore("branch instead of transaction") {
    val s = Var(1);
    val s2 = s.map(_-3);
    val s1 = s.map(_+3);
    
    val sall = for(v1 <- s1; v2 <- s2) yield (v1,v2)

    val allLog = sall.log

    assertResult(List((4,-2)))(allLog.now)

    s << 5

    assertResult(List((4,-2),(8,2)))(allLog.now)
  }
  
  test("Val and Routable") {
    val s1 = new Val(1)
    val s2 = new Val(2)
    val rs1 = RoutableVar(s1)
    val rs2 = RoutableVar(s2)

    val combined1 = for(v1 <- rs1; v2 <- s2) yield (v1,v2)
    val combined2 = for(v1 <- s1; v2 <- rs2) yield (v1,v2)

    assertResult((1,2))(combined1.now)
    assertResult((1,2))(combined2.now)

    rs2 << new Val(9)
    assertResult((1,9))(combined2.now)


    val s3 = Var(3)
    rs1 << s3
    assertResult((3,2))(combined1.now)

    s3 << 5
    assertResult((5,2))(combined1.now)
  }
}
