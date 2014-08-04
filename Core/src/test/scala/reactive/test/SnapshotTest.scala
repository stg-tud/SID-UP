package reactive.test
import reactive.signals.Var
import reactive.events.EventSource
import reactive.TransactionBuilder
import reactive.Lift.single._
import reactive.LiftableWrappers._
import org.scalatest.FunSuite

class SnapshotTest extends FunSuite {
  test("snapshotting works") {
    val var1 = Var(1)
    val events = EventSource[Object]
    val snapshot = var1.single.snapshot(events)
    val merged = add(var1, snapshot)

    val snapshotLog = snapshot.single.log
    val mergedLog = merged.single.log
    assertResult(List(1)) { snapshotLog.single.now }
    assertResult(List(2)) { mergedLog.single.now }

    var1 << 3
    assertResult(List(1)) { snapshotLog.single.now }
    assertResult(List(2, 4)) { mergedLog.single.now }

    var1 << 4
    assertResult(List(1)) { snapshotLog.single.now }
    assertResult(List(2, 4, 5)) { mergedLog.single.now }

    events << new Object
    assertResult(List(1, 4)) { snapshotLog.single.now }
    assertResult(List(2, 4, 5, 8)) { mergedLog.single.now }

    var1 << 6
    assertResult(List(1, 4)) { snapshotLog.single.now }
    assertResult(List(2, 4, 5, 8, 10)) { mergedLog.single.now }

    events << new Object
    assertResult(List(1, 4, 6)) { snapshotLog.single.now }
    assertResult(List(2, 4, 5, 8, 10, 12)) { mergedLog.single.now }

    events << new Object
    assertResult(List(1, 4, 6)) { snapshotLog.single.now }
    assertResult(List(2, 4, 5, 8, 10, 12)) { mergedLog.single.now }

    val transaction = new TransactionBuilder
    transaction.set(var1, 5)
    transaction.set(events, new Object)
    transaction.commit()
    assertResult(List(1, 4, 6, 5)) { snapshotLog.single.now }
    assertResult(List(2, 4, 5, 8, 10, 12, 10)) { mergedLog.single.now }

    events << new Object
    assertResult(List(1, 4, 6, 5)) { snapshotLog.single.now }
    assertResult(List(2, 4, 5, 8, 10, 12, 10)) { mergedLog.single.now }

    var1 << 9
    assertResult(List(1, 4, 6, 5)) { snapshotLog.single.now }
    assertResult(List(2, 4, 5, 8, 10, 12, 10, 14)) { mergedLog.single.now }

    transaction.set(var1, 9)
    transaction.set(events, new Object)
    transaction.commit()
    assertResult(List(1, 4, 6, 5, 9)) { snapshotLog.single.now }
    assertResult(List(2, 4, 5, 8, 10, 12, 10, 14, 18)) { mergedLog.single.now }

    var1 << 0
    assertResult(List(1, 4, 6, 5, 9)) { snapshotLog.single.now }
    assertResult(List(2, 4, 5, 8, 10, 12, 10, 14, 18, 9)) { mergedLog.single.now }
  }
}
