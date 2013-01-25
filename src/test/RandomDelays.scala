package test

import reactive.Signal.autoSignalToValue
import reactive.Var
import reactive.Signal
import reactive.Reactive
import java.text.SimpleDateFormat
import java.util.Date
import scala.actors.threadpool.AtomicInteger
import testtools.ReactiveLog

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
    val a1 = Signal("A1", s) { log("A1"); s % 2 };
    val a2 = Signal("A2", s) { log("A2"); s + 1 };
    val a3 = Signal("A3", s) { log("A3"); s + 1 };
    // this sums up to b1 = s % 2 + 1
    val b1 = Signal("B1", a1) { log("B1"); a1 + 1 };
    // this sums up to b2 = 2 * s + 2
    val b2 = Signal("B2", a2, a3) { log("B2"); a2 + a3 };
    // this sums up to c = s % 2 + 3 * s + 4
    val c = Signal("C", b1, a2, b2) { log("C"); b1 + a2 + b2 };

    val valueLog = new ReactiveLog(c);
    valueLog.assert(8);

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
    c.awaitValue(event);
    timeStampPrint("waiting period completed.");
    // sleep a bit more because the changed value might still have to be propagated to the log
    Thread.sleep(100);
    valueLog.assert(8, 10);

    println
    println("--------------------------")
    println

    timeStampPrint("updating source...");
    val event2 = s.set(4);
    timeStampPrint("waiting for propagation to complete...");
    c.awaitValue(event2)
    timeStampPrint("waiting period completed.");
    // sleep a bit more because the changed value might still have to be propagated to the log
    Thread.sleep(100);
    valueLog.assert(8, 10, 16);

    println
    println("--------------------------")
    println

    timeStampPrint("updating source twice ...");
    s.set(5);
    val event3 = s.set(7);
    timeStampPrint("waiting for propagation to complete...");
    c.awaitValue(event3)
    timeStampPrint("waiting period completed, terminating thread pool.");
    // sleep a bit more because the changed value might still have to be propagated to the log
    Thread.sleep(100);
    valueLog.assert(8, 10, 16, 20, 26);
  }

  //  println(s.toElaborateString);

  def track(signal: Signal[_]) {
    timeStampPrint("Now Tracking: " + signal.name + " = " + signal.now);
    signal.observe { value =>
      timeStampPrint("Value Changed: " + signal.name + " = " + value);
    }
  }
}