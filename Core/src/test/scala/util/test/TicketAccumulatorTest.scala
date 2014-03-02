package util.test

import org.scalatest.FunSuite
import util._
import org.scalatest.BeforeAndAfter

class TicketAccumulatorTest extends FunSuite with BeforeAndAfter {
  val accu = new TicketAccumulator();
  var result: TransactionAction = _
  def storeInResultField = { result = _ : TransactionAction }

  before {
    result = null;
  }

  test("No aggregation works") {
    accu.initializeForNotification(0)(storeInResultField);
    assertResult(COMMIT) { result }
  }
  test("Exception works") {
    accu.initializeForNotification(1)(storeInResultField);
    assertResult(null) { result }
    accu(COMMIT);
    assertResult(COMMIT) { result }
    intercept[IllegalStateException] {
      accu(COMMIT);
    }
  }
  test("Commit aggregation works") {
    accu.initializeForNotification(3)(storeInResultField);
    assertResult(null) { result }
    accu(COMMIT);
    assertResult(null) { result }
    accu(COMMIT);
    assertResult(null) { result }
    accu(COMMIT);
    assertResult(COMMIT) { result }
  }

  test("Retry overrides Commit") {
    accu.initializeForNotification(3)(storeInResultField);
    assertResult(null) { result }
    accu(COMMIT);
    assertResult(null) { result }
    accu(RETRY);
    assertResult(null) { result }
    accu(COMMIT);
    assertResult(RETRY) { result }
  }

  test("Abort overrides Commit") {
    accu.initializeForNotification(3)(storeInResultField);
    assertResult(null) { result }
    accu(COMMIT);
    assertResult(null) { result }
    accu(ABORT);
    assertResult(null) { result }
    accu(COMMIT);
    assertResult(ABORT) { result }
  }

  test("Abort overrides Retry") {
    accu.initializeForNotification(5)(storeInResultField);
    assertResult(null) { result }
    accu(COMMIT);
    assertResult(null) { result }
    accu(RETRY);
    assertResult(null) { result }
    accu(COMMIT);
    assertResult(null) { result }
    accu(ABORT);
    assertResult(null) { result }
    accu(COMMIT);
    assertResult(ABORT) { result }
  }

  test("Nesting works") {
    accu.initializeForNotification(2) (storeInResultField)
    val accu2 = new TicketAccumulator
    val accu3 = new TicketAccumulator
    accu2.initializeForNotification(3)(accu)
    accu3.initializeForNotification(0)(accu)

    assertResult(null) { result }
    accu2(COMMIT);
    assertResult(null) { result }
    accu2(RETRY);
    assertResult(null) { result }
    accu2(COMMIT);
    assertResult(RETRY) { result }
  }
}