package reactive.test
import reactive.events.EventSource
import reactive.TransactionBuilder
import org.scalatest.FunSuite
import reactive.signals.{Var, Val, TransposeSignal}
import reactive.signals.impl.FunctionalSignal
import scala.concurrent.stm._
import scala.collection.JavaConverters._

class ConcurrentTest extends FunSuite {

  val concurrencyFactor = 1000

  test("concurrency inside one transaction with MultiDependant") {
    val (start, end) = atomic { tx =>
      val start = Var(4)
      val intermediate = 1.to(concurrencyFactor).map(i => start.transactional.map(_ + 1)(tx))
      val combine: InTxn => Int = txi => intermediate.map {
        _.transactional.now(txi)
      }.sum
      val end = new FunctionalSignal(combine, intermediate.toSet, tx)
      (start, end)
    }
    assert(end.now == 5 * concurrencyFactor)

    start << 5
    assert(end.now == 6 * concurrencyFactor)

  }

  test("concurrency inside one transaction with DynamicDependant") {
    val (start, end) = atomic { tx =>
      val start = Var(4)
      val intermediate = 1.to(concurrencyFactor).map(i => start.transactional.map(_ + 1)(tx))
      val combine: InTxn => Int = txi => intermediate.map {
        _.transactional.now(txi)
      }.sum
      val transposed = new TransposeSignal(Val(intermediate), tx)
      val end = transposed.transactional.map(_.sum)(tx)
      (start, end)
    }
    assert(end.now == 5 * concurrencyFactor)

    start << 5
    assert(end.now == 6 * concurrencyFactor)

  }

  test("independent concurrent updates") {
    val vars = Vector.fill(concurrencyFactor)(Var(4)).par
    val results = atomic { tx => vars.map(_.transactional.map(_+1)(tx)) }
    results.foreach { res => assert( res.now == 5 )}

    vars.foreach(_ << 5)
    results.foreach { res => assert( res.now == 6 )}
  }

  test("concurrent updates on same var") {
    val v1 = Var(-1)
    val v2 = v1.map(_ + 1)
    val queue = new java.util.concurrent.ConcurrentLinkedQueue[Int]()
    v2.observe(v => queue.add(v))
    assert(queue.isEmpty)
    0.until(concurrencyFactor).par.foreach(i => v1 << i)
    assert(queue.size() == concurrencyFactor)
    queue.removeAll(1.to(concurrencyFactor).asJava)
    assert(queue.isEmpty)
  }
}
