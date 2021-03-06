package reactive.test

import reactive.signals.Var
import reactive.lifting.Lift._
import reactive.lifting.NumericLift._
import org.scalatest.FunSuite

class LateSignalCreationTest extends FunSuite {
  test("late creation works") {
    val var1 = Var(1);
    val var2 = Var(2);
    val var3 = Var(3);
    val signal = var1 + var2

    var1 << 3;
    var2 << 4;
    var3 << 5;

    val lateSignal = var3 + signal
    val log = lateSignal.log

    var1 << 2;
    var2 << 2;
    var3 << 2;

    assertResult(List(12, 11, 9, 6)) { log.now }
    println("cookie!");
  }
}
