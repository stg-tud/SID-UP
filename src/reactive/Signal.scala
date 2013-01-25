package reactive
import scala.collection.mutable
import java.util.UUID
import util.Util.nullSafeEqual
import impl.FunctionalSignal
import reactive.impl.SnapshotSignal
import reactive.impl.DependencyValueCache

trait Signal[+A] extends Reactive[A] {
  def now: A

  def awaitValue(event: Event): A

  def changes: EventStream[A]
  def apply[B](op: A => B): Signal[B] = new FunctionalSignal(name + "." + op, { op(this) }, this);
  def snapshot(when: EventStream[_]): Signal[A] = new SnapshotSignal(this, when);
}

object Signal {
  def apply[A](name: String, signals: Signal[_]*)(op: => A): Signal[A] = new FunctionalSignal[A](name, op, signals: _*);
  def apply[A](signals: Signal[_]*)(op: => A): Signal[A] = apply("AnonSignal", signals: _*)(op)

  implicit def autoSignalToValue[A](signal: Signal[A]): A = threadContext.get().get(signal);

  protected[reactive] val threadContext = new ThreadLocal[DependencyValueCache]()
  def withContext[A](context: DependencyValueCache)(op: => A) = {
    val old = threadContext.get();
    threadContext.set(context);
    try {
      op
    } finally {
      threadContext.set(old);
    }
  }

}