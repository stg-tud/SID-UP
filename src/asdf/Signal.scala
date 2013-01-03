package asdf
import scala.collection.mutable.MutableList

class Signal[A](name: String, op: => A, signals: Reactive[_]*) extends Reactive[A](name, op) {
  val level = signals.foldLeft(0)((max, signal) => math.max(max,signal.level + 1))

  signals.foreach { _.addDependant(this) }
  
  def newValue = op
}

object Signal {
  def apply[A](name: String, signals: Reactive[_]*)(op: => A) = new Signal[A](name, op, signals: _*);
  def apply[A](signals: Reactive[_]*)(op: => A) : Signal[A] = apply("AnonSignal", signals: _*)(op)
}