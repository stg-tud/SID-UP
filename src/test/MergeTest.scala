package test
import reactive.EventSource
import reactive.TransactionBuilder
import testtools.Asserts
import org.scalatest.FunSuite

class MergeTest extends FunSuite {
  test("merge event stream works") {
    val e1 = EventSource[Object]
    val e2 = EventSource[Int]
    val e3 = EventSource[Long]
    val merge = e1 merge (e2, e3)

    val mergeLog = merge.log

    e1 << "bla";
    e2 << 123;
    e3 << 5;
    val transaction = new TransactionBuilder
    transaction.set(e1, "x");
    transaction.set(e2, 2);
    transaction.commit();

    assert(List("bla", 123, 5, "x") === mergeLog.now flatMap { _ => List("bla", 123, 5, 2) === mergeLog.now })
  }
}