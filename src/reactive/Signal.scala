package reactive
import scala.collection.mutable
import java.util.UUID
import util.Util.nullSafeEqual

trait Signal[A] extends Reactive[A] {
  def value: A

  def await(event: Event)
//  def dirty: Signal[Boolean]

  def changes: EventStream[A]
  def snapshot(when : EventStream[_]) : Signal[A]
}

object Signal {
  implicit def autoSignalToValue[A](signal: Signal[A]): A = signal.value

  def apply[A](name: String, signals: Signal[_]*)(op: => A) = new FunctionalSignal[A](name, op, signals: _*);
  def apply[A](signals: Signal[_]*)(op: => A): Signal[A] = apply("AnonSignal", signals: _*)(op)

  protected[reactive] val threadEvent = new ThreadLocal[Event]() {
    override def initialValue = {
      null
    }
  }
  def during[A](event: Event)(op: => A) = {
    val old = threadEvent.get();
    threadEvent.set(event);
    try {
      op
    } finally {
      threadEvent.set(old);
    }
  }

}