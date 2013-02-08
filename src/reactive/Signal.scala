package reactive
import scala.collection.mutable
import java.util.UUID
import util.Util.nullSafeEqual
import impl.FunctionalSignal
import reactive.impl.SnapshotSignal
import scala.collection.Map
import scala.actors.threadpool.TimeoutException
import remote.RemoteSignal

trait Signal[+A] extends Reactive[A] with RemoteSignal[A] {
  // use this to get the current value from regular code
  def now: A
  // use this only inside FunctionalSignal closures
  // TODO should be package protected 
  def reactive(context : Signal.ReactiveEvaluationContext) : A
  
  // TODO should be package protected 
  def lastEvent: Event

  @throws(classOf[TimeoutException])
  def await(event: Event, timeout: Long = 0): A
  def renotify(dep : SignalDependant[A], event : Event)

  def changes: EventStream[A]
  def map[B](op: A => B): Signal[B]
  def rmap[B](op: A => Signal[B]): Signal[B]
  def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B];
  def snapshot(when: EventStream[_]): Signal[A]
}

object Signal {
  case class ReactiveEvaluationContext(event : Event, context : Map[Signal[_], Event]);
}
