package unoptimized

import java.util.UUID
import unoptimized.signals.Signal

trait Reactive[+O, +P] extends Reactive.Dependency {

  protected[unoptimized] def pulse(transaction: Transaction): Option[P]
  protected[unoptimized] def hasPulsed(transaction: Transaction): Boolean
  protected[unoptimized] def sourceDependenciesChanged(transaction: Transaction): Boolean 

  def log: Signal[Seq[O]]
  def observe(obs: O => Unit): Unit
  def unobserve(obs: O => Unit): Unit
}

object Reactive {
  trait Dependant {
    protected[unoptimized] def ping(transaction: Transaction): Unit
  }
  trait Dependency {
    protected[unoptimized] def sourceDependencies(transaction: Transaction): Set[UUID]
    protected[unoptimized] def isConnectedTo(transaction: Transaction): Boolean
    protected[unoptimized] def addDependant(transaction: Transaction, dependant: Dependant): Unit
    protected[unoptimized] def removeDependant(transaction: Transaction, dependant: Dependant): Unit
  }
  //  type RSeq[+A] = Reactive[Seq[A], Seq[A], Delta[A]]
  //  type Signal[+A] = Reactive[A, A, Update[A]]
  //  implicit def richSignal[A](signal: Signal[A]) = new Object {
  //    lazy val changes: EventStream[A] = new ChangesEventStream(this)
  //    def map[B](op: A => B): Signal[B] = new MapSignal(this, op)
  //    def flatMap[B](op: A => Signal[B]): Signal[B] = map(op).flatten
  //    def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B] = new FlattenSignal(this.asInstanceOf[Signal[Signal[B]]]);
  //    def log = new FoldSignal(List(now), changes, ((list: List[A], elem: A) => list :+ elem));
  //    def snapshot(when: EventStream[_]): Signal[A] = pulse(when).hold(now);
  //    def pulse(when: EventStream[_]): EventStream[A] = new PulseEventStream(this, when);
  //  }
  //  type EventStream[+A] = Reactive[A, Unit, Option[A]]
  //  implicit def richEventStream[A](eventStream: EventStream[A]) = new Object {
  //    def hold[B >: A](initialValue: B): Signal[B] = fold(initialValue){ (_, value) => value }
  //    def map[B](op: A => B): EventStream[B] = new MappedEventStream(this, op);
  //    def merge[B >: A](streams: EventStream[B]*): EventStream[B] = new MergeStream(this :: streams.toList);
  //    def fold[B](initialValue: B)(op: (B, A) => B): Signal[B] = new FoldSignal(initialValue, this, op);
  //    def log = fold(List[A]())((list, elem) => list :+ elem)
  //    def filter(op: A => Boolean): EventStream[A] = new FilteredEventStream(this, op);
  //  }
}