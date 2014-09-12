//package reactive.test
//import org.scalatest.FunSuite
//import reactive.signals.Var
//import reactive.testtools.MessageMixup
//import reactive.Lift
//import reactive.LiftableWrappers._
//import org.scalatest.BeforeAndAfter
//import reactive.signals.Signal
//import scala.concurrent._
//import ExecutionContext.Implicits.global
//import scala.util.Success
//import org.scalatest.Tag
//
//class TupleTest extends FunSuite with BeforeAndAfter {
//  def rtuple[A, B] = Lift.signal2((a: A, b: B) => (a, b))
//
//  var delayVar1: MessageMixup[Int] = _
//  var delayVar2: MessageMixup[String] = _
//  var tuple3: Signal[((Int, String), (Int, String))] = _
//  var log: Signal[List[((Int, String), (Int, String))]] = _
//
//  var futureVar1Set: Future[Unit] = _
//  var futureVar2Set: Future[Unit] = _
//  before {
//    val var1 = Var(1);
//    delayVar1 = new MessageMixup(var1);
//    val var2 = Var("a");
//    delayVar2 = new MessageMixup(var2);
//    val tuple1 = rtuple(var1, delayVar2);
//    val tuple2 = rtuple(delayVar1, var2);
//    tuple3 = rtuple(tuple1, tuple2);
//    log = tuple3.log
//    rprintln(tuple3);
//
//    futureVar1Set = future {
//      var1 << 2;
//    }
//    futureVar2Set = future {
//      var2 << "b";
//    }
//    Thread.sleep(100);
//  }
//
//  override def test(testName: String, testTags: Tag*)(testFun: => Unit) {
//    super.test(testName, testTags: _*) {
//      testFun
//      Thread.sleep(100);
//      assertResult(Some(Success(()))) { futureVar1Set.value }
//      assertResult(Some(Success(()))) { futureVar2Set.value }
//    }
//  }
//
//  ignore("release int first") {
//    delayVar1.releaseQueue();
//    delayVar2.releaseQueue();
//    assertResult(((2, "b"), (2, "b")), "erroneous endstate, value progression was: " + log.now) { tuple3.now }
//  }
//
//  ignore("release string first") {
//    delayVar2.releaseQueue();
//    delayVar1.releaseQueue();
//    assertResult(((2, "b"), (2, "b")), "erroneous endstate, value progression was: " + log.now) { tuple3.now }
//  }
//}
