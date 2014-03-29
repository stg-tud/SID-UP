package reactive.test

import reactive.signals.Var
import reactive.TransactionBuilder
import reactive.Lift._
import reactive.LiftableWrappers._
import org.scalatest.FunSuite

class TransactionTest extends FunSuite {
  test("transactions work") {
    val var1 = Var(1)
    val var2 = Var(5)

    val sum = add(var1, var2)
    val sumLog = sum.log

    var1 << 4
    assertResult(List(6, 9)) { sumLog.now }
    var2 << 4
    assertResult(List(6, 9, 8)) { sumLog.now }

    val transaction = TransactionBuilder()

    transaction.set(var1, 5)
      .set(var2, 5)
      .commit()
    assertResult(List(6, 9, 8, 10)) { sumLog.now }

    transaction.set(var1, 2)
      .set(var2, -2)
      .commit()
    assertResult(List(6, 9, 8, 10, 0)) { sumLog.now }

    var2 << 4
    assertResult(List(6, 9, 8, 10, 0, 6)) { sumLog.now }
  }
}
