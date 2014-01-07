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
    expectResult(COMMIT) { result }
  }
  test("Exception works") {
    accu.initializeForNotification(1)(storeInResultField);
    expectResult(null) { result }
    accu(COMMIT);
    expectResult(COMMIT) { result }
    intercept[IllegalStateException] {
      accu(COMMIT);
    }
  }
  test("Commit aggregation works") {
    accu.initializeForNotification(3)(storeInResultField);
    expectResult(null) { result }
    accu(COMMIT);
    expectResult(null) { result }
    accu(COMMIT);
    expectResult(null) { result }
    accu(COMMIT);
    expectResult(COMMIT) { result }
  }

  test("Retry overrides Commit") {
    accu.initializeForNotification(3)(storeInResultField);
    expectResult(null) { result }
    accu(COMMIT);
    expectResult(null) { result }
    accu(RETRY);
    expectResult(null) { result }
    accu(COMMIT);
    expectResult(RETRY) { result }
  }

  test("Abort overrides Commit") {
    accu.initializeForNotification(3)(storeInResultField);
    expectResult(null) { result }
    accu(COMMIT);
    expectResult(null) { result }
    accu(ABORT);
    expectResult(null) { result }
    accu(COMMIT);
    expectResult(ABORT) { result }
  }

  test("Abort overrides Retry") {
    accu.initializeForNotification(5)(storeInResultField);
    expectResult(null) { result }
    accu(COMMIT);
    expectResult(null) { result }
    accu(RETRY);
    expectResult(null) { result }
    accu(COMMIT);
    expectResult(null) { result }
    accu(ABORT);
    expectResult(null) { result }
    accu(COMMIT);
    expectResult(ABORT) { result }
  }

  test("Nesting works") {
    accu.initializeForNotification(2) (storeInResultField)
    val accu2 = new TicketAccumulator
    val accu3 = new TicketAccumulator
    accu2.initializeForNotification(3)(accu)
    accu3.initializeForNotification(0)(accu)

    expectResult(null) { result }
    accu2(COMMIT);
    expectResult(null) { result }
    accu2(RETRY);
    expectResult(null) { result }
    accu2(COMMIT);
    expectResult(RETRY) { result }
  }
}