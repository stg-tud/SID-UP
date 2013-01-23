package test
import reactive.EventSource
import reactive.Transaction
import testtools.ReactiveLog

object MergeTest extends App {
  val e1 = EventSource[Object]
  val e2 = EventSource[Int]
  val e3 = EventSource[Long]
  val merge = e1 merge (e2, e3)
  
  val mergeLog = new ReactiveLog(merge);
  
  e1 << "bla";
  e2 << 123;
  e3 << 5;
  val transaction = new Transaction
  transaction.set(e1, "x");
  transaction.set(e2, 2);
  transaction.commit();
  
  try{
    mergeLog.assert("bla", 123, 5, "x");
  } catch {
    case _ : ReactiveLog.AssertionFailure => mergeLog.assert("bla", 123, 5, 2);
  }
}