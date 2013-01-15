package example

import reactive.Reactive.autoSignalToValue
import reactive.Var
import reactive.Signal
import reactive.Reactive

object Test extends App {
  def log(name: String) {
    println("Starting evaluation of Signal " + name);
    Thread.sleep(500);
    println("Finished evaluation of Signal " + name);
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

    println
    println("-------------")
    println

    track(s);
    track(a1);
    track(a2);
    track(a3);
    track(b1);
    track(b2);
    track(c);

    println
    println("-------------")
    println

    println("updating source...");
    val event = s.set(2)
    println("waiting for propagation to complete...");
    c.await(event);
    println("waiting period completed.");

    println
    println("-------------")
    println

    println("updating source...");
    val event2 = s.set(4);
    println("waiting for propagation to complete...");
    c.await(event2)
    println("waiting period completed, terminating thread pool.");
  }

  //  println(s.toElaborateString);

  def track(signal: Reactive[_]) {
    println("Now Tracking: " + signal.name + " = " + signal.value);
    signal.observe {
      println("Value Changed: " + signal.name + " = " + signal.value);
    }
  }
}