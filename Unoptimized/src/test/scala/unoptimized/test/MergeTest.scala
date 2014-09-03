package unoptimized.test
import unoptimized.events.EventSource
import unoptimized.TransactionBuilder
import org.scalatest.FunSuite
import unoptimized.signals.Var

class MergeTest extends FunSuite {
  test("merge event stream works") {
    val e1 = EventSource[Object]
    val e2 = EventSource[Int]
    val e3 = EventSource[Long]
    val merge = e1 merge[Any] (e2, e3)

    val mergeLog = merge.log

    e1 << "bla"
    e2 << 123
    e3 << 5
    val transaction = new TransactionBuilder
    transaction.set(e1, "x")
    transaction.set(e2, 2)
    transaction.commit()

    assert((List("bla", 123, 5, "x") === mergeLog.now) || (List("bla", 123, 5, 2) === mergeLog.now))
  }

  test("merge signal change streams") {
    val sx = Var(1)
    val sy = Var(2)
    val merged = sx.changes.merge(sy.changes)
  }

  test("merge signal changes and eventstream") {
    val sx = Var(1)
    assertResult(Set(sx.uuid)) { sx.sourceDependencies(null) }
    val cx = sx.changes
    assertResult(Set(sx.uuid)) { cx.sourceDependencies(null) }
    val e1 = EventSource[Int]
    val merged = e1.merge(cx)
  }
}
