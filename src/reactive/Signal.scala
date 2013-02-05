package reactive
import scala.collection.mutable
import java.util.UUID
import util.Util.nullSafeEqual
import impl.FunctionalSignal
import reactive.impl.SnapshotSignal
import scala.collection.Map
import scala.actors.threadpool.TimeoutException

// TODO should be package protected 
trait Signal[+A] extends Reactive[A] {
  // use this to get the current value from regular code
  def now: A
  // use this only inside FunctionalSignal closures
  // TODO should be package protected 
  def reactive: A
  
  // TODO should be package protected 
  def lastEvent: Event

  @throws(classOf[TimeoutException])
  def await(event: Event, timeout: Long = 0): A

  def changes: EventStream[A]
  def map[B](op: A => B): Signal[B]
  def snapshot(when: EventStream[_]): Signal[A]
}

// TODO this should all be removed in favor of reactive.Lift'ed functions 
object Signal {
  // use this only inside FunctionalSignal closures
  implicit def autoSignalToValue[A](a: Signal[A]): A = a.reactive
  def apply[A](name: String, signals: Signal[_]*)(op: => A): Signal[A] = new FunctionalSignal[A](name, op, signals: _*);
  def apply[A](signals: Signal[_]*)(op: => A): Signal[A] = apply("AnonSignal", signals: _*)(op)
}