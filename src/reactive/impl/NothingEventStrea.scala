package reactive.impl
import reactive.EventStream
import java.util.UUID
import reactive.Event
import reactive.EventStreamDependant
import reactive.Signal

object NothingEventStream extends EventStream[Nothing] {
  override val sourceDependencies = Map[UUID, UUID]()
  override val name = "NothingEventStream"
  override def await(event: Event, timeout: Long = 0): Option[Nothing] = throw new IllegalArgumentException("NothingEventStream cannot emit");
  override def addDependant(obs: EventStreamDependant[Nothing]) = {}
  override def removeDependant(obs: EventStreamDependant[Nothing]) = {}
  override def observe(obs: Nothing => Unit) = {}
  override def unobserve(obs: Nothing => Unit) = {}
  
  override def hold[B >: Nothing](initialValue: B): Signal[B] = new Val(initialValue)
  override def map[B](op: Nothing => B): EventStream[B] = this
  override def merge[B >: Nothing](streams: EventStream[B]*): EventStream[B] = if (streams.length == 1) streams.head else streams.head.merge(streams.tail: _*)
  override def fold[B](initialValue: B)(op: (B, Nothing) => B): Signal[B] = new Val(initialValue)
  override val log: Signal[List[Nothing]] = new Val(List[Nothing]())
  override def filter(op: Nothing => Boolean): EventStream[Nothing] = this
}
