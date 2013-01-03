package asdf

import Reactive.autoSignalToValue

object Test extends App {
  val bottom = Var("bottom", 1);
  val left = Signal("left[bottom+1]", bottom) { bottom + 1 };
  val right = Signal("right[bottom+2]", bottom) { bottom + 2 }
  val top = Signal("top[2*bottom+3]", left, right) { left + right }

  track(bottom);
  track(top);

  bottom.set(3);
  bottom.set(3);

  println(bottom);

  def track(signal: Reactive[_]) {
    println("tracking " + signal.name + " = " + signal.value);
    signal.observe {
      println("changed: " + signal.name + " = " + signal.value);
    }
  }
}