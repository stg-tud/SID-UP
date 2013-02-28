package locks
import org.scalatest.FunSuite

class LockTest extends FunSuite {
  test("initial read lock works") {
    expectResult(true) { new TransactionReentrantReadWriteLock[Int].readLock.lockOrFail(1) }
  }
  test("initial write lock works") {
    expectResult(true) { new TransactionReentrantReadWriteLock[Int].writeLock.lockOrFail(1) }
  }
  test("write lock is exclusive") {
    val lock = new TransactionReentrantReadWriteLock[Int]
    expectResult(true) { lock.writeLock.lockOrFail(1) }
    expectResult(false) { lock.writeLock.lockOrFail(2) }
    lock.writeLock.release(1)
    expectResult(true) { lock.writeLock.lockOrFail(2) }
    expectResult(false) { lock.writeLock.lockOrFail(1) }
  }

  test("write lock blocks other read locks") {
    val lock = new TransactionReentrantReadWriteLock[Int]
    expectResult(true) { lock.writeLock.lockOrFail(1) }
    expectResult(false) { lock.readLock.lockOrFail(2) }
    lock.writeLock.release(1)
    expectResult(true) { lock.readLock.lockOrFail(2) }
  }

  test("read lock blocks other write locks") {
    val lock = new TransactionReentrantReadWriteLock[Int]
    expectResult(true) { lock.readLock.lockOrFail(1) }
    expectResult(false) { lock.writeLock.lockOrFail(2) }
    lock.readLock.release(1)
    expectResult(true) { lock.writeLock.lockOrFail(2) }
  }

  test("write lock is re-entrant") {
    val lock = new TransactionReentrantReadWriteLock[Int]
    expectResult(true) { lock.writeLock.lockOrFail(1) }
    expectResult(false) { lock.writeLock.lockOrFail(2) }
    expectResult(true) { lock.writeLock.lockOrFail(1) }
    expectResult(false) { lock.writeLock.lockOrFail(2) }
    expectResult(true) { lock.writeLock.lockOrFail(1) }
    expectResult(false) { lock.writeLock.lockOrFail(2) }

    lock.writeLock.release(1)
    expectResult(false) { lock.writeLock.lockOrFail(2) }
    lock.writeLock.release(1)
    expectResult(false) { lock.writeLock.lockOrFail(2) }
    lock.writeLock.release(1)
    expectResult(true) { lock.writeLock.lockOrFail(2) }
  }

  test("read lock is re-entrant") {
    val lock = new TransactionReentrantReadWriteLock[Int]
    expectResult(true) { lock.readLock.lockOrFail(1) }
    expectResult(false) { lock.writeLock.lockOrFail(2) }
    expectResult(true) { lock.readLock.lockOrFail(1) }
    expectResult(false) { lock.writeLock.lockOrFail(2) }
    expectResult(true) { lock.readLock.lockOrFail(1) }
    expectResult(false) { lock.writeLock.lockOrFail(2) }

    lock.readLock.release(1)
    expectResult(false) { lock.writeLock.lockOrFail(2) }
    lock.readLock.release(1)
    expectResult(false) { lock.writeLock.lockOrFail(2) }
    lock.readLock.release(1)
    expectResult(true) { lock.writeLock.lockOrFail(2) }
  }

  test("read lock is non-exclusive") {
    val lock = new TransactionReentrantReadWriteLock[Int]
    expectResult(true) { lock.readLock.lockOrFail(1) }
    expectResult(true) { lock.readLock.lockOrFail(2) }
    expectResult(true) { lock.readLock.lockOrFail(3) }
    expectResult(true) { lock.readLock.lockOrFail(3) }
    expectResult(false) { lock.writeLock.lockOrFail(2) }
    lock.readLock.release(1);
    lock.readLock.release(2);
    lock.readLock.release(3);
    expectResult(false) { lock.writeLock.lockOrFail(2) }
    lock.readLock.release(3);
    expectResult(true) { lock.writeLock.lockOrFail(2) }
  }
}