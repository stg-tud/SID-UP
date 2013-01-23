package test
import reactive.Var
import reactive.EventSource
import reactive.Signal
import reactive.Signal.autoSignalToValue
import reactive.Transaction
import reactive.Reactive

object SnapshotTest extends App {
  val var1 = Var(1);
  val events = EventSource[Object]
  val snapshot = var1.snapshot(events);
  val merged = Signal(var1, snapshot) {
    var1 + snapshot;
  }
  val snapshotLog = new ReactiveLog(snapshot);
  val mergedLog = new ReactiveLog(merged);
  snapshotLog.assert(1);
  mergedLog.assert(2);

  var1.set(3);
  snapshotLog.assert(1);
  mergedLog.assert(2, 4);

  var1.set(4);
  snapshotLog.assert(1);
  mergedLog.assert(2, 4, 5);

  events << new Object
  snapshotLog.assert(1, 4);
  mergedLog.assert(2, 4, 5, 8);

  var1.set(6);
  snapshotLog.assert(1, 4);
  mergedLog.assert(2, 4, 5, 8, 10);

  events << new Object
  snapshotLog.assert(1, 4, 6);
  mergedLog.assert(2, 4, 5, 8, 10, 12);

  events << new Object
  snapshotLog.assert(1, 4, 6);
  mergedLog.assert(2, 4, 5, 8, 10, 12);

  val transaction = new Transaction
  transaction.set(var1, 5);
  transaction.set(events, new Object);
  transaction.commit();
  snapshotLog.assert(1, 4, 6, 5);
  mergedLog.assert(2, 4, 5, 8, 10, 12, 10);

  events << new Object
  snapshotLog.assert(1, 4, 6, 5);
  mergedLog.assert(2, 4, 5, 8, 10, 12, 10);

  var1.set(0);
  snapshotLog.assert(1, 4, 6, 5);
  mergedLog.assert(2, 4, 5, 8, 10, 12, 10, 5);
}