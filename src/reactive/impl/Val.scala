package reactive.impl
import reactive.Signal
import reactive.Event
import scala.actors.threadpool.TimeoutException
import reactive.EventStream
import java.util.UUID
import reactive.EventStreamDependant
import reactive.SignalDependant

class Val[A](value: A) extends Signal[A] {
  override val name = String.valueOf(value)
  override val now = value
  override def reactive(context: Signal.ReactiveEvaluationContext) = value
  override def lastEvent: Event = null
  override def await(event: Event, timeout: Long = 0): A = throw new IllegalArgumentException("Val cannot update");
  override val sourceDependencies = Map[UUID, UUID]()
  override def isConnectedTo(event: Event): Boolean = false
  override def addDependant(obs: SignalDependant[A]) = {}
  override def removeDependant(obs: SignalDependant[A]) = {}
  override def observe(obs: A => Unit) = {}
  override def unobserve(obs: A => Unit) = {}
  override val changes: EventStream[A] = NothingEventStream
  override def map[B](op: A => B): Signal[B] = new Val(op(value))
  override def rmap[B](op: A => Signal[B]): Signal[B] = op(value);
  override def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B] = value
  override lazy val log = new Val(List(value))
  override def snapshot(when: EventStream[_]): Signal[A] = this
}
