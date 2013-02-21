package test
import org.scalatest.FunSuite
import reactive.Var
import testtools.MessageMixup
import reactive.Lift
import reactive.LiftableWrappers._
import org.scalatest.BeforeAndAfter
import reactive.Signal

class TupleTest extends FunSuite with BeforeAndAfter {
  def rtuple[A, B] = Lift.signal2((a: A, b: B) => (a, b))

  var delayVar1: MessageMixup[Int] = _
  var delayVar2: MessageMixup[String] = _
  var tuple3: Signal[((Int, String), (Int, String))] = _
  var log: Signal[List[((Int, String), (Int, String))]] = _

  before {
    println("setup..");
    val var1 = Var(1);
    delayVar1 = new MessageMixup(var1);
    val var2 = Var("a");
    delayVar2 = new MessageMixup(var2);
    val tuple1 = rtuple(var1, delayVar2);
    val tuple2 = rtuple(delayVar1, var2);
    tuple3 = rtuple(tuple1, tuple2);
    log = tuple3.log
    rprintln(tuple3);

    var1.set(2);
    var2.set("b");
  }

  test("release int first") {
    println("run: release int first");
    delayVar1.releaseQueue();
    delayVar2.releaseQueue();
    expectResult(((2, "b"), (2, "b")), "erroneous endstate, value progression was: " + log.now) { tuple3.now }
  }

  test("release string first") {
    println("run: release string first");
    delayVar2.releaseQueue();
    delayVar1.releaseQueue();
    expectResult(((2, "b"), (2, "b")), "erroneous endstate, value progression was: " + log.now) { tuple3.now }
  }
}