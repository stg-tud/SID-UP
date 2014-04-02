package dctm.locks

import org.scalatest.FunSuite

class LockTest extends FunSuite {
  test("initial read lock works") {
    expectResult(true) { new TransactionReentrantReadWriteLock[Int].readLock.tryLock(1) }
  }
  test("initial write lock works") {
    expectResult(true) { new TransactionReentrantReadWriteLock[Int].writeLock.tryLock(1) }
  }
  test("initially unlocked") {
    val lock = new TransactionReentrantReadWriteLock[Nothing]
    expectResult(true) { lock.readLock.isFree }
    expectResult(true) { lock.writeLock.isFree }
    expectResult(true) { lock.isFree }
  }
  
  test("write lock is exclusive") {
    val lock = new TransactionReentrantReadWriteLock[Int]
    expectResult(true) { lock.writeLock.tryLock(1) }
    expectResult(false) { lock.writeLock.tryLock(2) }
    lock.writeLock.release(1)
    expectResult(true) { lock.writeLock.tryLock(2) }
    expectResult(false) { lock.writeLock.tryLock(1) }
  }

  test("write lock blocks other read locks") {
    val lock = new TransactionReentrantReadWriteLock[Int]
    expectResult(true) { lock.writeLock.tryLock(1) }
    expectResult(false) { lock.readLock.tryLock(2) }
    lock.writeLock.release(1)
    expectResult(true) { lock.readLock.tryLock(2) }
  }

  test("read lock blocks other write locks") {
    val lock = new TransactionReentrantReadWriteLock[Int]
    expectResult(true) { lock.readLock.tryLock(1) }
    expectResult(false) { lock.writeLock.tryLock(2) }
    lock.readLock.release(1)
    expectResult(true) { lock.writeLock.tryLock(2) }
  }

  test("write lock is re-entrant") {
    val lock = new TransactionReentrantReadWriteLock[Int]
    expectResult(true) { lock.writeLock.isFree }
    expectResult(true) { lock.writeLock.tryLock(1) }
    expectResult(false) { lock.writeLock.isFree }
    expectResult(false) { lock.writeLock.tryLock(2) }
    expectResult(true) { lock.writeLock.tryLock(1) }
    expectResult(false) { lock.writeLock.isFree }
    expectResult(false) { lock.writeLock.tryLock(2) }
    expectResult(true) { lock.writeLock.tryLock(1) }
    expectResult(false) { lock.writeLock.isFree }
    expectResult(false) { lock.writeLock.tryLock(2) }

    lock.writeLock.release(1)
    expectResult(false) { lock.writeLock.isFree }
    expectResult(false) { lock.writeLock.tryLock(2) }
    lock.writeLock.release(1)
    expectResult(false) { lock.writeLock.isFree }
    expectResult(false) { lock.writeLock.tryLock(2) }
    lock.writeLock.release(1)
    expectResult(true) { lock.writeLock.isFree }
    expectResult(true) { lock.writeLock.tryLock(2) }
  }

  test("read lock is re-entrant") {
    val lock = new TransactionReentrantReadWriteLock[Int]
    expectResult(true) { lock.readLock.tryLock(1) }
    expectResult(false) { lock.writeLock.tryLock(2) }
    expectResult(true) { lock.readLock.tryLock(1) }
    expectResult(false) { lock.writeLock.tryLock(2) }
    expectResult(true) { lock.readLock.tryLock(1) }
    expectResult(false) { lock.writeLock.tryLock(2) }

    lock.readLock.release(1)
    expectResult(false) { lock.writeLock.tryLock(2) }
    lock.readLock.release(1)
    expectResult(false) { lock.writeLock.tryLock(2) }
    lock.readLock.release(1)
    expectResult(true) { lock.writeLock.tryLock(2) }
  }

  test("read lock is non-exclusive") {
    val lock = new TransactionReentrantReadWriteLock[Int]
    expectResult(true) { lock.readLock.isFree }
    expectResult(true) { lock.readLock.tryLock(1) }
    expectResult(false) { lock.readLock.isFree }
    expectResult(true) { lock.readLock.tryLock(2) }
    expectResult(false) { lock.readLock.isFree }
    expectResult(true) { lock.readLock.tryLock(3) }
    expectResult(false) { lock.readLock.isFree }
    expectResult(true) { lock.readLock.tryLock(3) }
    expectResult(false) { lock.readLock.isFree }
    expectResult(false) { lock.writeLock.tryLock(2) }
    lock.readLock.release(1);
    expectResult(false) { lock.readLock.isFree }
    lock.readLock.release(2);
    expectResult(false) { lock.readLock.isFree }
    lock.readLock.release(3);
    expectResult(false) { lock.readLock.isFree }
    expectResult(false) { lock.writeLock.tryLock(2) }
    lock.readLock.release(3);
    expectResult(true) { lock.readLock.isFree }
    expectResult(true) { lock.writeLock.tryLock(2) }
  }
}