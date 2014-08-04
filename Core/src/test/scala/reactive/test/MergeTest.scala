package reactive.test
import reactive.events.EventSource
import reactive.TransactionBuilder
import org.scalatest.FunSuite
import reactive.signals.Var

class MergeTest extends FunSuite {
  test("merge event stream works") {
    val e1 = EventSource[Object]()
    val e2 = EventSource[Int]()
    val e3 = EventSource[Long]()
    val merge = e1.single.merge[Any](e2, e3)

    val mergeLog = merge.single.log

    e1 << "bla"
    e2 << 123
    e3 << 5
    val transaction = new TransactionBuilder
    transaction.set(e1, "x")
    transaction.set(e2, 2)
    transaction.commit()

    println(mergeLog.single.now)

    assert((List("bla", 123, 5, "x") === mergeLog.single.now) || (List("bla", 123, 5, 2) === mergeLog.single.now))
  }

  test("merge signal change streams") {
    val sx = Var(1)
    val sy = Var(2)
    val merged = scala.concurrent.stm.atomic { implicit tx => sx.changes.merge(sy.changes) }
  }

  test("merge signal changes and eventstream") {
    val sx = Var(1)
    assertResult(Set(sx.uuid)) { sx.single.sourceDependencies }
    val cx = sx.single.changes
    assertResult(Set(sx.uuid)) { cx.single.sourceDependencies }
    val e1 = EventSource[Int]()
    val merged = e1.single.merge(cx)
  }
}
