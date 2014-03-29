//package reactive.test
//import reactive.signals.Var
//import reactive.events.EventSource
//import reactive.TransactionBuilder
//import reactive.Lift._
//import reactive.LiftableWrappers._
//import org.scalatest.FunSuite
//
//class SnapshotTest extends FunSuite {
//  test("snapshotting works") {
//    val var1 = Var(1);
//    val events = EventSource[Object]
//    val snapshot = var1.snapshot(events);
//    val merged = add(var1, snapshot);
//
//    val snapshotLog = snapshot.log
//    val mergedLog = merged.log
//    assertResult(List(1)) { snapshotLog.now };
//    assertResult(List(2)) { mergedLog.now };
//
//    var1 << 3;
//    assertResult(List(1)) { snapshotLog.now };
//    assertResult(List(2, 4)) { mergedLog.now };
//
//    var1 << 4;
//    assertResult(List(1)) { snapshotLog.now };
//    assertResult(List(2, 4, 5)) { mergedLog.now };
//
//    events << new Object
//    assertResult(List(1, 4)) { snapshotLog.now };
//    assertResult(List(2, 4, 5, 8)) { mergedLog.now };
//
//    var1 << 6;
//    assertResult(List(1, 4)) { snapshotLog.now };
//    assertResult(List(2, 4, 5, 8, 10)) { mergedLog.now };
//
//    events << new Object
//    assertResult(List(1, 4, 6)) { snapshotLog.now };
//    assertResult(List(2, 4, 5, 8, 10, 12)) { mergedLog.now };
//
//    events << new Object
//    assertResult(List(1, 4, 6)) { snapshotLog.now };
//    assertResult(List(2, 4, 5, 8, 10, 12)) { mergedLog.now };
//
//    val transaction = new TransactionBuilder
//    transaction.set(var1, 5);
//    transaction.set(events, new Object);
//    transaction.commit();
//    assertResult(List(1, 4, 6, 5)) { snapshotLog.now };
//    assertResult(List(2, 4, 5, 8, 10, 12, 10)) { mergedLog.now };
//
//    events << new Object
//    assertResult(List(1, 4, 6, 5)) { snapshotLog.now };
//    assertResult(List(2, 4, 5, 8, 10, 12, 10)) { mergedLog.now };
//
//    var1 << 9;
//    assertResult(List(1, 4, 6, 5)) { snapshotLog.now };
//    assertResult(List(2, 4, 5, 8, 10, 12, 10, 14)) { mergedLog.now };
//
//    transaction.set(var1, 9);
//    transaction.set(events, new Object)
//    transaction.commit()
//    assertResult(List(1, 4, 6, 5, 9)) { snapshotLog.now };
//    assertResult(List(2, 4, 5, 8, 10, 12, 10, 14, 18)) { mergedLog.now };
//
//    var1 << 0;
//    assertResult(List(1, 4, 6, 5, 9)) { snapshotLog.now };
//    assertResult(List(2, 4, 5, 8, 10, 12, 10, 14, 18, 9)) { mergedLog.now };
//  }
//}
