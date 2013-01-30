package reactive
import scala.collection.mutable
import java.util.UUID
import util.Util.nullSafeEqual
import impl.FunctionalSignal
import reactive.impl.SnapshotSignal
import scala.collection.Map
import scala.actors.threadpool.TimeoutException

trait Signal[+A] extends Reactive[A] {
  def now: A
  protected def value(ifKnown: Event, otherwise: => Event): A
  def lastEvent: Event
  @throws(classOf[TimeoutException])
  def await(event: Event, timeout: Long = 0): A

  def changes: EventStream[A]
  def apply[B](op: A => B): Signal[B] = changes.map(op).hold(op(now))
  def fold[B](initial: A => B)(op: (B, A) => B): Signal[B]
  override def log = fold(List(_))((list, elem) => list :+ elem);
  def snapshot(when: EventStream[_]): Signal[A] = new SnapshotSignal(this, when);
}

object Signal {
  def apply[A](name: String, signals: Signal[_]*)(op: => A): Signal[A] = new FunctionalSignal[A](name, op, signals: _*);
  def apply[A](signals: Signal[_]*)(op: => A): Signal[A] = apply("AnonSignal", signals: _*)(op)

  implicit def autoSignalToValue[A](signal: Signal[A]): A = {
    val (event, context) = threadContext.get();
    signal.value(event, { context.get(signal).getOrElse(null) })
  }

  protected[reactive] val threadContext = new ThreadLocal[(Event, Map[Signal[_], Event])]()
  def withContext[A](event: Event, context: Map[Signal[_], Event])(op: => A) = {
    val old = threadContext.get();
    threadContext.set((event, context));
    try {
      op
    } finally {
      threadContext.set(old);
    }
  }

}