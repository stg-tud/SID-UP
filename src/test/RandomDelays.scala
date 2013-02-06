package test

import reactive.Var
import reactive.Signal
import reactive.Reactive
import reactive.Lift._
import java.text.SimpleDateFormat
import java.util.Date
import scala.actors.threadpool.AtomicInteger
import testtools.Asserts

object RandomDelays extends App {
  val format = new SimpleDateFormat("[mm:ss.SSS] ")
  def timeStampPrint(text: String) {
    println(format.format(new Date) + text)
  }

  val id = new AtomicInteger(0)
  def log(name: String) {
    val uid = id.incrementAndGet();
    val duration = 250 + (math.random * 500).toInt
    timeStampPrint("Starting evaluation of Signal %s [%02d]: %d ms".format(name, uid, duration));
    Thread.sleep(duration);
    timeStampPrint("Finished evaluation of Signal %s [%02d]".format(name, uid, duration));
  }

  Reactive.withThreadPoolSize(4) {
    val s = Var("S", 1);

    def modulo(name : String) = { (x: Int, y : Int) => log(name); x % y };
    def add(name : String) = { (x: Int, y : Int) => log(name); x + y };
    
    val bla = signal2(modulo("A1"))
    val a1 = bla(s, 2)
    val a2 = signal2(add("A2"))(s, 1)
    val a3 = signal2(add("A3"))(s, 1);
    // this sums up to b1 = s % 2 + 1
    val b1 = signal2(add("B1"))(a1, 1);
    // this sums up to b2 = 2 * s + 2
    val b2 = signal2(add("B2"))(a2, a3);
    // this sums up to c = s % 2 + 3 * s + 4
    val C = { (x1: Int, x2: Int, x3: Int) => log("C"); x1 + x2 + x3 };
    val c = C(b1, a2, b2)

    val valueLog = c.log
    Asserts.assert(List(8), valueLog.now);

    println
    println("--------------------------")
    println

    track(s);
    track(a1);
    track(a2);
    track(a3);
    track(b1);
    track(b2);
    track(c);

    println
    println("--------------------------")
    println

    timeStampPrint("updating source...");
    val event = s.set(2)
    timeStampPrint("waiting for propagation to complete...");
    c.await(event);
    timeStampPrint("waiting period completed.");
    // sleep a bit more because the changed value might still have to be propagated to the log
    Thread.sleep(100);
    Asserts.assert(List(8, 10), valueLog.now);

    println
    println("--------------------------")
    println

    timeStampPrint("updating source...");
    val event2 = s.set(4);
    timeStampPrint("waiting for propagation to complete...");
    c.await(event2)
    timeStampPrint("waiting period completed.");
    // sleep a bit more because the changed value might still have to be propagated to the log
    Thread.sleep(100);
    Asserts.assert(List(8, 10, 16), valueLog.now);

    println
    println("--------------------------")
    println

    timeStampPrint("updating source twice ...");
    s.set(5);
    val event3 = s.set(7);
    timeStampPrint("waiting for propagation to complete...");
    c.await(event3)
    timeStampPrint("waiting period completed, terminating thread pool.");
    // sleep a bit more because the changed value might still have to be propagated to the log
    Thread.sleep(100);
    Asserts.assert(List(8, 10, 16, 20, 26), valueLog.now);
  }

  //  println(s.toElaborateString);

  def track(signal: Signal[AnyVal]) {
    // semi-implicit lifting of sink function (semi-implicit because scala
    // can't figure it out if you don't store every step in a variable)
    val log = { value: AnyVal => timeStampPrint("Value Changed: " + signal.name + " = " + value); }
    val logLifted: Signal[AnyVal] => Unit = log

    timeStampPrint("Now Tracking: " + signal.name);
    logLifted(signal);
  }
}