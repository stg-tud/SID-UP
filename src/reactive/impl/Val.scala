package reactive.impl
import reactive.Signal
import reactive.Event
import scala.actors.threadpool.TimeoutException
import reactive.EventStream
import java.util.UUID
import reactive.ReactiveDependant

class Val[A](value: A) extends Signal[A] {
  override val name = String.valueOf(value)
  override val now = value
  override def reactive(context : Signal.ReactiveEvaluationContext) = value
  override def lastEvent: Event = null
  override def await(event: Event, timeout: Long = 0): A = throw new IllegalArgumentException("val can not update");
  override val sourceDependencies = Map[UUID, UUID]()
  override def isConnectedTo(event: Event): Boolean = false
  override def addDependant(obs: ReactiveDependant[A]) = {}
  override def removeDependant(obs: ReactiveDependant[A]) = {}
  override def observe(obs: A => Unit) = {}
  override def unobserve(obs: A => Unit) = {}
  override lazy val changes: EventStream[A] = Val.NothingEventStream
  override def map[B](op: A => B): Signal[B] = new Val(op(value))
  override lazy val log = new Val(List(value))
  override def snapshot(when: EventStream[_]): Signal[A] = this

}

object Val {
  object NothingEventStream extends EventStream[Nothing] {
    override val sourceDependencies = Map[UUID, UUID]()
    override val name = "VoidEventStream"
    override def await(event: Event, timeout: Long = 0): Option[Nothing] = throw new IllegalArgumentException("val can not update");
    override def addDependant(obs: ReactiveDependant[Nothing]) = {}
    override def removeDependant(obs: ReactiveDependant[Nothing]) = {}
    override def observe(obs: Nothing => Unit) = {}
    override def unobserve(obs: Nothing => Unit) = {}
    override def hold[B >: Nothing](initialValue: B): Signal[B] = new Val(initialValue)
    override def map[B](op: Nothing => B): EventStream[B] = this
    override def merge[B >: Nothing](streams: EventStream[B]*): EventStream[B] = if (streams.length == 1) streams.head else streams.head.merge(streams.tail: _*)
    override def fold[B](initialValue: B)(op: (B, Nothing) => B): Signal[B] = new Val(initialValue)
    override lazy val log: Signal[List[Nothing]] = new Val(List[Nothing]())
    override def filter(op: Nothing => Boolean): EventStream[Nothing] = this
  }
}