package test
import reactive.Var
import testtools.Asserts
import reactive.Transaction

object RMapTest extends App {
  var invocationCount = 0;
  val var1 = Var(3);
  val var2 = Var(5);
  val foo = Var(var1);
  val bar = foo.rmap { x => invocationCount += 1; x };
  Asserts.assert(3, bar.now);
  Asserts.assert(1, invocationCount);

  var1.set(4);
  Asserts.assert(4, bar.now);
  Asserts.assert(2, invocationCount);
  
  foo.set(var2);
  Asserts.assert(5, bar.now);
  Asserts.assert(3, invocationCount);
  
  var1.set(7);
  Asserts.assert(5, bar.now);
  Asserts.assert(3, invocationCount);
  
  var2.set(6);
  Asserts.assert(6, bar.now);
  Asserts.assert(4, invocationCount);

  val t = new Transaction;
  t.set(var2, 0);
  t.set(foo, var1);
  t.commit();
  Asserts.assert(7, bar.now);
  Asserts.assert(5, invocationCount);

  t.set(var1, 0);
  t.set(var2, 8);
  t.set(foo, var2);
  t.commit();
  Asserts.assert(8, bar.now);
  Asserts.assert(6, invocationCount);

  var1.set(10);
  Asserts.assert(8, bar.now);
  Asserts.assert(6, invocationCount);

  var2.set(9);
  Asserts.assert(9, bar.now);
  Asserts.assert(7, invocationCount);

  foo.set(var1);
  Asserts.assert(10, bar.now);
  Asserts.assert(8, invocationCount);
}