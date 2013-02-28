package reactive.impl
import reactive.Signal
import reactive.Transaction
import reactive.EventStream
import java.util.UUID
import remote.RemoteReactiveDependant

class Val[A](value: A) extends Signal[A] {
  override val name = String.valueOf(value)
  override val now = value
  override val sourceDependencies = Set[UUID]()
  override def isConnectedTo(event: Transaction): Boolean = false
  override def addDependant(obs: RemoteReactiveDependant[A]) = {}
  override def removeDependant(obs: RemoteReactiveDependant[A]) = {}
  override def observe(obs: A => Unit) = {}
  override def unobserve(obs: A => Unit) = {}
  override val changes: EventStream[A] = NothingEventStream
  override def map[B](op: A => B): Signal[B] = new Val(op(value))
  override def rmap[B](op: A => Signal[B]): Signal[B] = op(value);
  override def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B] = value
  override lazy val log = new Val(List(value))
  override def snapshot(when: EventStream[_]): Signal[A] = this
  override def renotify(dep : RemoteReactiveDependant[A], event : Transaction) = throw new UnsupportedOperationException
}
