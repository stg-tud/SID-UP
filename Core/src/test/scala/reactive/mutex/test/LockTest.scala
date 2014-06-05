package reactive.mutex.test

import org.scalatest.FunSuite
import scala.concurrent._
import scala.concurrent.duration._
import reactive.mutex.TransactionLock
import reactive.mutex.TransactionLockImpl
import java.util.UUID
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.Executors

class LockTest extends FunSuite {
  implicit val executionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  test("Repeated Acquisition Works") {
    val lock: TransactionLock = new TransactionLockImpl()
    val one = UUID.randomUUID()
    val two = UUID.randomUUID()
    assert(lock.owner === None)
    lock.acquire(one)
    assert(lock.owner === Some(one))
    lock.release(one)
    assert(lock.owner === None)
    lock.acquire(two)
    assert(lock.owner === Some(two))
    lock.release(two)
    assert(lock.owner === None)
    lock.acquire(two)
    assert(lock.owner === Some(two))
    lock.release(two)
    assert(lock.owner === None)
    lock.acquire(one)
    assert(lock.owner === Some(one))
    lock.release(one)
    assert(lock.owner === None)
    lock.acquire(two)
    assert(lock.owner === Some(two))
    lock.release(two)
    assert(lock.owner === None)
  }

  test("Mutex works") {
    val lock: TransactionLock = new TransactionLockImpl()
    val one = UUID.randomUUID()
    val two = UUID.randomUUID()
    var acquired = false
    lock.acquire(one)
    assert(lock.owner === Some(one))
    val acq = Future {
      lock.acquire(two)
      acquired = true
    }

    intercept[TimeoutException] {
      Await.ready(acq, Duration(100, MILLISECONDS))
    }
    assert(acq.isCompleted === false)
    assert(acquired === false)
    assert(lock.owner === Some(one))

    lock.release(one)

    Await.ready(acq, Duration(100, MILLISECONDS))
    assert(acq.isCompleted === true)
    assert(acquired === true)
    assert(lock.owner === Some(two))
  }

  test("Reentrance works") {
    val lock: TransactionLock = new TransactionLockImpl()
    val one = UUID.randomUUID()
    val two = UUID.randomUUID()

    assert(lock.owner === None)

    lock.acquire(one)
    assert(lock.owner === Some(one))
    lock.acquire(one)
    assert(lock.owner === Some(one))
    lock.release(one)
    assert(lock.owner === Some(one))
    lock.release(one)

    assert(lock.owner === None)

    lock.acquire(two)
    assert(lock.owner === Some(two))
    lock.acquire(two)
    assert(lock.owner === Some(two))
    lock.acquire(two)
    assert(lock.owner === Some(two))
    lock.release(two)
    assert(lock.owner === Some(two))
    lock.release(two)
    assert(lock.owner === Some(two))
    lock.release(two)
    assert(lock.owner === None)
  }

  test("Reentant Mutex works") {
    val lock: TransactionLock = new TransactionLockImpl()
    val one = UUID.randomUUID()
    val two = UUID.randomUUID()
    var acquired = false
    lock.acquire(one)
    lock.acquire(one)
    assert(lock.owner === Some(one))
    val acq = Future {
      lock.acquire(two)
      lock.acquire(two)
      lock.acquire(two)
      acquired = true
    }

    intercept[TimeoutException] {
      Await.ready(acq, Duration(100, MILLISECONDS))
    }
    assert(acq.isCompleted === false)
    assert(acquired === false)
    assert(lock.owner === Some(one))

    lock.release(one)

    intercept[TimeoutException] {
      Await.ready(acq, Duration(100, MILLISECONDS))
    }
    assert(acq.isCompleted === false)
    assert(acquired === false)
    assert(lock.owner === Some(one))

    lock.release(one)

    Await.ready(acq, Duration(100, MILLISECONDS))
    assert(acq.isCompleted === true)
    assert(acquired === true)
    assert(lock.owner === Some(two))

    lock.release(two)
    assert(lock.owner === Some(two))
    lock.release(two)
    assert(lock.owner === Some(two))
    lock.release(two)
    assert(lock.owner === None)
  }

  test("cannot free foreign locks") {
    val lock: TransactionLock = new TransactionLockImpl()
    val one = UUID.randomUUID()
    val two = UUID.randomUUID()

    assert(lock.owner === None)
    lock.acquire(one)
    assert(lock.owner === Some(one))
    try {
      lock.release(two)
    } catch {
      case e: Exception => // ignore
    }
    assert(lock.owner === Some(one))
    lock.release(one)
    assert(lock.owner === None)
  }

  test("cannot under-release locks") {
    val lock: TransactionLock = new TransactionLockImpl()
    val one = UUID.randomUUID()

    assert(lock.owner === None)
    try {
      lock.release(one)
    } catch {
      case e: Exception => // ignore
    }
    assert(lock.owner === None)
    lock.acquire(one)
    assert(lock.owner === Some(one))
    lock.release(one)
    assert(lock.owner === None)
    try {
      lock.release(one)
    } catch {
      case e: Exception => // ignore
    }
    assert(lock.owner === None)
  }

  test("multiple waiting threads all pass through") {
    val sync = new Object
    val lock: TransactionLock = new TransactionLockImpl()
    val one = UUID.randomUUID()
    val two = UUID.randomUUID()
    val three = UUID.randomUUID()
    lock.acquire(one)

    assert(lock.owner === Some(one))

    val future21 = Future { lock.acquire(two) }
    val future31 = Future { lock.acquire(three) }
    val future22 = Future { lock.acquire(two) }
    val future32 = Future { lock.acquire(three) }
    val future23 = Future { lock.acquire(two) }
    val future33 = Future { lock.acquire(three) }
    val future24 = Future { lock.acquire(two) }
    val future34 = Future { lock.acquire(three) }

    lock.release(one)
    Thread.sleep(100)

    assert(lock.owner !== None)
    val nextOwner = lock.owner match {
      case Some(`two`) =>
        assert(future21.isCompleted === true)
        assert(future22.isCompleted === true)
        assert(future23.isCompleted === true)
        assert(future24.isCompleted === true)
        assert(future31.isCompleted === false)
        assert(future32.isCompleted === false)
        assert(future33.isCompleted === false)
        assert(future34.isCompleted === false)
        lock.release(two)
        assert(lock.owner === Some(two))
        lock.release(two)
        assert(lock.owner === Some(two))
        lock.release(two)
        assert(lock.owner === Some(two))
        lock.release(two)
        three
      case Some(`three`) =>
        assert(future21.isCompleted === false)
        assert(future22.isCompleted === false)
        assert(future23.isCompleted === false)
        assert(future24.isCompleted === false)
        assert(future31.isCompleted === true)
        assert(future32.isCompleted === true)
        assert(future33.isCompleted === true)
        assert(future34.isCompleted === true)
        lock.release(three)
        assert(lock.owner === Some(three))
        lock.release(three)
        assert(lock.owner === Some(three))
        lock.release(three)
        assert(lock.owner === Some(three))
        lock.release(three)
        two
      case somethingElse =>
        fail("lock owner should be " + Some(one) + " or " + Some(two) + ", but is " + somethingElse)
    }
    Thread.sleep(100)

    assert(lock.owner === Some(nextOwner))
    assert(future21.isCompleted === true)
    assert(future22.isCompleted === true)
    assert(future23.isCompleted === true)
    assert(future24.isCompleted === true)
    assert(future31.isCompleted === true)
    assert(future32.isCompleted === true)
    assert(future33.isCompleted === true)
    assert(future34.isCompleted === true)
    lock.release(nextOwner)
    assert(lock.owner === Some(nextOwner))
    lock.release(nextOwner)
    assert(lock.owner === Some(nextOwner))
    lock.release(nextOwner)
    assert(lock.owner === Some(nextOwner))
    lock.release(nextOwner)
    Thread.sleep(100)

    assert(lock.owner === None)
  }
}
