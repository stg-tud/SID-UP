package reactive;
package signals;

import reactive.events.EventStream
import java.util.UUID
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds 
import scala.collection.TraversableLike

trait SettableSignal[A] extends Signal[A]

object SettableSignal {
  def apply[A](initialValue: A): SettableSignal[A] = new SettableSignal[A] {
    val _input = Var(Set[EventStream[A]]());
    def <<+(setEvents: EventStream[A]) = _input << _input.now + setEvents
    def <<-(setEvents: EventStream[A]) = _input << _input.now - setEvents

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
  }
}