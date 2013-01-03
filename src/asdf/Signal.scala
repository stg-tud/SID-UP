package asdf
import scala.collection.mutable.MutableList

class Signal[A](name: String, op: => A, signals: Reactive[_]*) extends Reactive[A](name, op) {
  val level = signals.foldLeft(0)((max, signal) => math.max(max,signal.level + 1))

  private var dirtydeps = 0;
  signals.foreach { x =>
    x.addObserver(this)
    if (x.isDirty) dirtydeps += 1;
  }

  def notifyDirty() {
    dirtydeps += 1;
    dirty
  }

  private var anyChanged = false;
  def notifyClean(changed : Boolean) {
    dirtydeps -= 1;
    anyChanged |= changed;
    if (anyChanged && dirtydeps == 0) newValueAndClean(op)
  }
}

object Signal {
  def apply[A](name: String, signals: Reactive[_]*)(op: => A) = new Signal[A](name, op, signals: _*);
  def apply[A](signals: Reactive[_]*)(op: => A) : Signal[A] = apply("AnonSignal", signals: _*)(op)
}