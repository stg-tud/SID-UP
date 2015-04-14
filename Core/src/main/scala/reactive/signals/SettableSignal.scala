package reactive;
package signals;

import reactive.events.EventStream
import java.util.UUID
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.collection.TraversableLike
import reactive.remote.impl.RemoteSignalPublisher
import reactive.remote.RemoteSignalDependency
import reactive.remote.impl.RemoteSignalSubscriber
import java.io.ObjectStreamException

@remote trait RemoteIncrementalSource[B] {
  def <<+(value: B): Unit
  def <<-(value: B): Unit
  def swap(out: B, in: B): Unit
}
trait IncrementalSource[B] {
  def <<+(value: B): Unit
  def <<-(value: B): Unit
  def swap(out: B, in: B): Unit
}
trait SettableSignal[A] extends Signal[A] with IncrementalSource[EventStream[A]]

object SettableSignal {
  def apply[A](initialValue: A): SettableSignal[A] = new SettableSignal[A] with Serializable {
    val _input = Var(Set[EventStream[A]]());
    override def <<+(event: EventStream[A]) = _input << _input.now + event
    override def <<-(event: EventStream[A]) = _input << _input.now - event
    override def swap(out: EventStream[A], in: EventStream[A]): Unit = _input << _input.now - out + in

    val _output = _input.transposeE.map(_.head).hold(initialValue)
    protected[reactive] override def value(transaction: Transaction): A = _output.value(transaction)
    protected[reactive] override def pulse(transaction: Transaction): Option[A] = _output.pulse(transaction)
    protected[reactive] override def hasPulsed(transaction: Transaction): Boolean = _output.hasPulsed(transaction)
    protected[reactive] override def sourceDependencies(transaction: Transaction): Set[UUID] = _output.sourceDependencies(transaction)
    protected[reactive] override def isConnectedTo(transaction: Transaction): Boolean = _output.isConnectedTo(transaction);
    protected[reactive] override def addDependant(transaction: Transaction, dependant: Reactive.Dependant) = _output.addDependant(transaction, dependant)
    protected[reactive] override def removeDependant(transaction: Transaction, dependant: Reactive.Dependant) = _output.removeDependant(transaction, dependant)
    override def log: Signal[Seq[A]] = _output.log
    override def observe(obs: A => Unit) = _output.observe(obs)
    override def unobserve(obs: A => Unit) = _output.unobserve(obs)
    override def now: A = _output.now
    override def changes: EventStream[A] = _output.changes
    override def delta: EventStream[(A, A)] = _output.delta
    override def map[B](op: A => B): Signal[B] = _output.map(op)
    override def flatMap[B](op: A => Signal[B]): Signal[B] = _output.flatMap(op)
    override def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B] = _output.flatten
    override def snapshot(when: EventStream[_]): Signal[A] = _output.snapshot(when)
    override def pulse(when: EventStream[_]): EventStream[A] = _output.pulse(when)
    override def transposeS[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[Signal[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]]) = _output.transposeS
    override def transposeE[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[EventStream[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]]): EventStream[C[T]] = _output.transposeE
    override def ===(other: Signal[_]): Signal[Boolean] = _output.===(other)

    private lazy val remote = new RemoteSettableSignalPublisher(this)
    @throws(classOf[ObjectStreamException])
    protected def writeReplace(): Any = AutoRemoteSettableSignal(remote)
  }

  case class AutoRemoteSettableSignal[A](wrapped: RemoteSettableSignalDependency[A]) {
    @throws(classOf[ObjectStreamException])
    def readResolve(): Any = {
      new RemoteSettableSignalSubscriber(wrapped)
    }
  }
}

@remote trait RemoteSettableSignalDependency[A] extends RemoteSignalDependency[A] with RemoteIncrementalSource[EventStream[A]]

class RemoteSettableSignalPublisher[A](val local: SettableSignal[A]) extends RemoteSignalPublisher[A](local) with RemoteSettableSignalDependency[A] {
  override def <<+(value: EventStream[A]): Unit = local <<+ value
  override def <<-(value: EventStream[A]): Unit = local <<- value
  override def swap(out: EventStream[A], in: EventStream[A]): Unit = local.swap(out, in)
}

class RemoteSettableSignalSubscriber[A](val remote: RemoteSettableSignalDependency[A]) extends RemoteSignalSubscriber[A](remote) with SettableSignal[A] {
  override def <<+(value: EventStream[A]): Unit = remote <<+ value
  override def <<-(value: EventStream[A]): Unit = remote <<- value
  override def swap(out: EventStream[A], in: EventStream[A]): Unit = remote.swap(out, in)
}