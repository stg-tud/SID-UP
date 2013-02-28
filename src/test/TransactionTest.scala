package test
import reactive.Var
import reactive.Signal
import reactive.TransactionBuilder
import testtools.Asserts
import reactive.Lift._
import reactive.LiftableWrappers._
import org.scalatest.FunSuite

class TransactionTest extends FunSuite {
  test("transactions work") {
    val var1 = Var(1);
    val var2 = Var(5);

    val sum = add(var1, var2)
    val sumLog = sum.log

    var1.set(4);
    var2.set(4);

    val transaction = new TransactionBuilder();

    transaction.set(var1, 5);
    transaction.set(var2, 5);
    transaction.commit();

    transaction.set(var1, 2);
    transaction.set(var2, -2);
    transaction.commit();

    var2.set(4);

    expectResult(List(6, 9, 8, 10, 0, 6)) { sumLog.now }
  }
}