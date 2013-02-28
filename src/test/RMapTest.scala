package test
import org.scalatest.FunSuite
import reactive.Var
import testtools.Asserts
import reactive.TransactionBuilder

class RMapTest extends FunSuite {
  test("rmap works") {
    var invocationCount = 0;
    val var1 = Var(3);
    val var2 = Var(5);
    val foo = Var(var1);
    val bar = foo.rmap { x => invocationCount += 1; x };
    expectResult(3) { bar.now }
    expectResult(1) { invocationCount }

    var1.set(4);
    expectResult(4) { bar.now }
    expectResult(1) { invocationCount }

    foo.set(var2);
    expectResult(5) { bar.now }
    expectResult(2) { invocationCount }

    var1.set(7);
    expectResult(5) { bar.now }
    expectResult(2) { invocationCount }

    var2.set(6);
    expectResult(6) { bar.now }
    expectResult(2) { invocationCount }

    val t = new TransactionBuilder;
    t.set(var2, 0);
    t.set(foo, var1);
    t.commit();
    expectResult(7) { bar.now }
    expectResult(3) { invocationCount }

    t.set(var1, 0);
    t.set(var2, 8);
    t.set(foo, var2);
    t.commit();
    expectResult(8) { bar.now }
    expectResult(4) { invocationCount }

    var1.set(10);
    expectResult(8) { bar.now }
    expectResult(4) { invocationCount }

    var2.set(9);
    expectResult(9) { bar.now }
    expectResult(4) { invocationCount }

    foo.set(var1);
    expectResult(10) { bar.now }
    expectResult(5) { invocationCount }
  }
}