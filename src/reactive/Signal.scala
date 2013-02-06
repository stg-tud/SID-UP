package reactive
import scala.collection.mutable
import java.util.UUID
import util.Util.nullSafeEqual
import impl.FunctionalSignal
import reactive.impl.SnapshotSignal
import scala.collection.Map
import scala.actors.threadpool.TimeoutException

trait Signal[+A] extends Reactive[A] {
  // use this to get the current value from regular code
  def now: A
  // use this only inside FunctionalSignal closures
  // TODO should be package protected 
  def reactive(context : Signal.ReactiveEvaluationContext) : A
  
  // TODO should be package protected 
  def lastEvent: Event

  @throws(classOf[TimeoutException])
  def await(event: Event, timeout: Long = 0): A

  def changes: EventStream[A]
  def map[B](op: A => B): Signal[B]
  def rmap[R <: Signal[_]](op : A => R) : R
  def snapshot(when: EventStream[_]): Signal[A]
}

object Signal {
  case class ReactiveEvaluationContext(event : Event, context : Map[Signal[_], Event]);
}
