package test
import reactive.Var
import reactive.EventSource
import reactive.Signal
import reactive.Transaction
import reactive.Reactive
import reactive.Lift._
import reactive.LiftableWrappers._
import org.scalatest.FunSuite

class SnapshotTest extends FunSuite {
  test("snapshotting works") {
    val var1 = Var(1);
    val events = EventSource[Object]
    val snapshot = var1.snapshot(events);
    val merged = add(var1, snapshot);

    val snapshotLog = snapshot.log
    val mergedLog = merged.log
    expectResult(List(1)) { snapshotLog.now };
    expectResult(List(2)) { mergedLog.now };

    var1.set(3);
    expectResult(List(1)) { snapshotLog.now };
    expectResult(List(2, 4)) { mergedLog.now };

    var1.set(4);
    expectResult(List(1)) { snapshotLog.now };
    expectResult(List(2, 4, 5)) { mergedLog.now };

    events << new Object
    expectResult(List(1, 4)) { snapshotLog.now };
    expectResult(List(2, 4, 5, 8)) { mergedLog.now };

    var1.set(6);
    expectResult(List(1, 4)) { snapshotLog.now };
    expectResult(List(2, 4, 5, 8, 10)) { mergedLog.now };

    events << new Object
    expectResult(List(1, 4, 6)) { snapshotLog.now };
    expectResult(List(2, 4, 5, 8, 10, 12)) { mergedLog.now };

    events << new Object
    expectResult(List(1, 4, 6)) { snapshotLog.now };
    expectResult(List(2, 4, 5, 8, 10, 12)) { mergedLog.now };

    val transaction = new Transaction
    transaction.set(var1, 5);
    transaction.set(events, new Object);
    transaction.commit();
    expectResult(List(1, 4, 6, 5)) { snapshotLog.now };
    expectResult(List(2, 4, 5, 8, 10, 12, 10)) { mergedLog.now };

    events << new Object
    expectResult(List(1, 4, 6, 5)) { snapshotLog.now };
    expectResult(List(2, 4, 5, 8, 10, 12, 10)) { mergedLog.now };

    var1.set(9);
    expectResult(List(1, 4, 6, 5)) { snapshotLog.now };
    expectResult(List(2, 4, 5, 8, 10, 12, 10, 14)) { mergedLog.now };

    transaction.set(var1, 9);
    transaction.set(events, new Object)
    transaction.commit()
    expectResult(List(1, 4, 6, 5, 9)) { snapshotLog.now };
    expectResult(List(2, 4, 5, 8, 10, 12, 10, 14, 18)) { mergedLog.now };

    var1.set(0);
    expectResult(List(1, 4, 6, 5, 9)) { snapshotLog.now };
    expectResult(List(2, 4, 5, 8, 10, 12, 10, 14, 18, 9)) { mergedLog.now };
  }
}