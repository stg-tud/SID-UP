package test
import reactive.Var
import reactive.EventSource
import reactive.Signal
import reactive.Signal.autoSignalToValue
import reactive.Transaction
import reactive.Reactive
import testtools.Asserts

object SnapshotTest extends App {
  val var1 = Var(1);
  val events = EventSource[Object]
  val snapshot = var1.snapshot(events);
  val merged = Signal(var1, snapshot) {
    var1 + snapshot;
  }
  val snapshotLog = snapshot.log
  val mergedLog = merged.log
  Asserts.assert(List(1), snapshotLog.now);
  Asserts.assert(List(2), mergedLog.now);

  var1.set(3);
  Asserts.assert(List(1), snapshotLog.now);
  Asserts.assert(List(2, 4), mergedLog.now);

  var1.set(4);
  Asserts.assert(List(1), snapshotLog.now);
  Asserts.assert(List(2, 4, 5), mergedLog.now);

  events << new Object
  Asserts.assert(List(1, 4), snapshotLog.now);
  Asserts.assert(List(2, 4, 5, 8), mergedLog.now);

  var1.set(6);
  Asserts.assert(List(1, 4), snapshotLog.now);
  Asserts.assert(List(2, 4, 5, 8, 10), mergedLog.now);

  events << new Object
  Asserts.assert(List(1, 4, 6), snapshotLog.now);
  Asserts.assert(List(2, 4, 5, 8, 10, 12), mergedLog.now);

  events << new Object
  Asserts.assert(List(1, 4, 6), snapshotLog.now);
  Asserts.assert(List(2, 4, 5, 8, 10, 12), mergedLog.now);

  val transaction = new Transaction
  transaction.set(var1, 5);
  transaction.set(events, new Object);
  transaction.commit();
  Asserts.assert(List(1, 4, 6, 5), snapshotLog.now);
  Asserts.assert(List(2, 4, 5, 8, 10, 12, 10), mergedLog.now);

  events << new Object
  Asserts.assert(List(1, 4, 6, 5), snapshotLog.now);
  Asserts.assert(List(2, 4, 5, 8, 10, 12, 10), mergedLog.now);

  var1.set(9);
  Asserts.assert(List(1, 4, 6, 5), snapshotLog.now);
  Asserts.assert(List(2, 4, 5, 8, 10, 12, 10, 14), mergedLog.now);
  
  transaction.set(var1, 9);
  transaction.set(events, new Object)
  transaction.commit()
  Asserts.assert(List(1, 4, 6, 5, 9), snapshotLog.now);
  Asserts.assert(List(2, 4, 5, 8, 10, 12, 10, 14, 18), mergedLog.now);
  
  var1.set(0);
  Asserts.assert(List(1, 4, 6, 5, 9), snapshotLog.now);
  Asserts.assert(List(2, 4, 5, 8, 10, 12, 10, 14, 18, 9), mergedLog.now);
}