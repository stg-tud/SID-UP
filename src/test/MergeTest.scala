package test
import reactive.EventSource
import reactive.Transaction
import testtools.Asserts

object MergeTest extends App {
  val e1 = EventSource[Object]
  val e2 = EventSource[Int]
  val e3 = EventSource[Long]
  val merge = e1 merge (e2, e3)
  
  val mergeLog = merge.log
  
  e1 << "bla";
  e2 << 123;
  e3 << 5;
  val transaction = new Transaction
  transaction.set(e1, "x");
  transaction.set(e2, 2);
  transaction.commit();
  
  try{
    Asserts.assert(List("bla", 123, 5, "x"), mergeLog.now);
  } catch {
    case _ : Asserts.AssertionFailure => Asserts.assert(List("bla", 123, 5, 2), mergeLog.now);
  }
}