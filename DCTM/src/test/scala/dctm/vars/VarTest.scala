package dctm.vars

import org.scalatest.FunSuite
import scala.concurrent._
import duration._
import ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Sorting
import java.util.concurrent.atomic.AtomicInteger

class VarTest extends FunSuite {
  test("normal read works") {
    val myVar = new TransactionalVariable[Int, Int](123)
    expectResult(123) {
      TestTransactionExecutor.retryUntilSuccess { implicit t =>
        myVar.get
      }
    }
    expectResult(123) { myVar.lastCommitted }
    expectResult(true) { myVar.isFree }
  }

  test("normal transform works") {
    val myVar = new TransactionalVariable[Int, Int](123)
    expectResult(123 -> 246) {
      TestTransactionExecutor.retryUntilSuccess { implicit t =>
        myVar.transform(_ * 2)
      }
    }
    expectResult(246) { myVar.lastCommitted }
    expectResult(true) { myVar.isFree }
  }

  test("retry read works") {
    val myVar = new TransactionalVariable[Int, Int](123)
    future {
      TestTransactionExecutor.retryUntilSuccess { implicit t =>
        myVar.transform(_ * 2)
        Thread.sleep(50)
      }
    }
    Thread.sleep(10)
    var retries = 0
    expectResult(246) {
      TestTransactionExecutor.retryUntilSuccess { implicit t =>
        retries += 1
        myVar.get
      }
    }
    expectResult(true) { retries > 1 }
    expectResult(true) { myVar.isFree }
  }

  test("high load transforms work") {
    val spawns = 5
    val myVar = new TransactionalVariable[Int, Int](0)
    val retries = new AtomicInteger(0)
    val successes = new AtomicInteger(0)
    val results = (1 to spawns) map { _ =>
      future {
        TestTransactionExecutor.retryUntilSuccess { implicit t =>
          retries.getAndIncrement()
          val result = myVar.transform(_ + 1)
          Thread.sleep(1)
          successes.getAndIncrement()
          result
        }
      }
    }
    val actual = results.map(Await.result(_, 1.second))
    expectResult(true) { retries.get() > successes.get }
    val actualSorted = actual.toArray
    Sorting.quickSort(actualSorted)
    val expected = (1 to spawns) map { i => (i - 1, i) }
    expectResult(expected.toList)(actualSorted.toList)
  }
}