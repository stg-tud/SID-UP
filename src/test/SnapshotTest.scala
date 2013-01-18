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
  // initial (1, 1+1 = 2)

  var1.set(3);
  // now (1, 1+3 = 4)
  var1.set(4);
  // now (1, 1+4 = 5)
  events << new Object
  // now (1, 4+4 = 8)
  var1.set(6);
  // now (1, 4+6 = 10)
  events << new Object
  // now (1, 6+6 = 12)
  events << new Object
  // remain (1, 6+6 = 12)

  val transaction = new Transaction
  transaction.set(var1, 5);
  transaction.set(events, new Object);
  transaction.commit();
  // now (1, 5+5 = 10)

  events << new Object
  // remain (1, 5+5 = 10)

  var1.set(0);
  // now (1, 0+5 = 10)

  snapshotLog.assert(1, 4, 6, 5);
  mergedLog.assert(2, 4, 5, 8, 10, 12, 10, 5);
}