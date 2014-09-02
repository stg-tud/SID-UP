package unoptimized.test
import unoptimized.signals.Var
import unoptimized.TransactionBuilder
import unoptimized.Lift._
import unoptimized.LiftableWrappers._
import org.scalatest.FunSuite

class TransactionTest extends FunSuite {
  test("transactions work") {
    val var1 = Var(1);
    val var2 = Var(5);

    val sum = add(var1, var2)
    val sumLog = sum.log

    var1 << 4;
    assertResult(List(6, 9)) { sumLog.now }
    var2 << 4;
    assertResult(List(6, 9, 8)) { sumLog.now }

    val transaction = new TransactionBuilder();

    transaction.set(var1, 5);
    transaction.set(var2, 5);
    transaction.commit();
    assertResult(List(6, 9, 8, 10)) { sumLog.now }

    transaction.set(var1, 2);
    transaction.set(var2, -2);
    transaction.commit();
    assertResult(List(6, 9, 8, 10, 0)) { sumLog.now }

    var2 << 4;
    assertResult(List(6, 9, 8, 10, 0, 6)) { sumLog.now }
  }
}
