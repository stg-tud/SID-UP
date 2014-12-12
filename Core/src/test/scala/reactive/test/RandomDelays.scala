package reactive.test

import org.scalatest.FunSuite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger
import reactive.signals.Signal
import reactive.Lift._
import reactive.signals.Var

class RandomDelays extends FunSuite {
  val format = new SimpleDateFormat("[mm:ss.SSS] ")
  def timeStampPrint(text: String): Unit = {
    println(format.format(new Date) + text)
  }

  val id = new AtomicInteger(0)
  def log(name: String): Unit = {
    val uid = id.incrementAndGet()
    val duration = 250 + (math.random * 500).toInt
    timeStampPrint(f"Starting evaluation of Signal $name [$uid%02d]: $duration ms")
    Thread.sleep(duration)
    timeStampPrint(f"Finished evaluation of Signal $name [$uid%02d]")
  }
  def track(signal: Signal[AnyVal]): Unit = {
    // semi-implicit lifting of sink function (semi-implicit because scala
    // can't figure it out if you don't store every step in a variable)
    val log = { value: AnyVal => timeStampPrint("Value Changed: " + signal + " = " + value); }
    val logLifted: Signal[AnyVal] => Unit = log

    timeStampPrint("Now Tracking: " + signal)
    logLifted(signal)
  }

  test("randomly delayed messages don't screw up evaluation order") {
    //    TransactionExecutor.withThreadPoolSize(4) {
    val s = Var(1)

    def modulo(name: String) = { (x: Int, y: Int) => log(name); x % y }
    def add(name: String) = { (x: Int, y: Int) => log(name); x + y }

    val a1 = signal2(modulo("A1"))(s, 2)
    val a2 = signal2(add("A2"))(s, 1)
    val a3 = signal2(add("A3"))(s, 1)
    // this sums up to b1 = s % 2 + 1
    val b1 = signal2(add("B1"))(a1, 1)
    // this sums up to b2 = 2 * s + 2
    val b2 = signal2(add("B2"))(a2, a3)
    // this sums up to c = s % 2 + 3 * s + 4
    val C = { (x1: Int, x2: Int, x3: Int) => log("C"); x1 + x2 + x3 }
    val c = C(b1, a2, b2)

    val valueLog = c.log
    assertResult(List(8)) { valueLog.now }

    println()
    println("--------------------------")
    println()

    track(s)
    track(a1)
    track(a2)
    track(a3)
    track(b1)
    track(b2)
    track(c)

    println()
    println("--------------------------")
    println()

    timeStampPrint("updating source...")
    s << 2
    timeStampPrint("waiting for propagation to complete...")
    Thread.sleep(10)
    timeStampPrint("waiting period completed.")
    // sleep a bit more because the changed value might still have to be propagated to the log
    Thread.sleep(100)
    assertResult(List(8, 10)) { valueLog.now }

    println()
    println("--------------------------")
    println()

    timeStampPrint("updating source...")
    s << 4
    timeStampPrint("waiting for propagation to complete...")
    Thread.sleep(10)
    timeStampPrint("waiting period completed.")
    // sleep a bit more because the changed value might still have to be propagated to the log
    Thread.sleep(100)
    assertResult(List(8, 10, 16)) { valueLog.now }

    println()
    println("--------------------------")
    println()

    timeStampPrint("updating source twice ...")
    s << 5
    s << 7
    timeStampPrint("waiting for propagation to complete...")
    Thread.sleep(10)
    timeStampPrint("waiting period completed, terminating thread pool.")
    // sleep a bit more because the changed value might still have to be propagated to the log
    Thread.sleep(100)
    assertResult(List(8, 10, 16, 20, 26)) { valueLog.now }
    //    }
  }
}
