package reactive

import java.util.UUID
import reactive.signals.Signal

trait Reactive[+O, +P] extends Reactive.Dependency with Reactive.Observable[O] {

  protected[reactive] def pulse(transaction: Transaction): Pulse[P]
  protected[reactive] def hasPulsed(transaction: Transaction): Boolean

}

object Reactive {
  trait Dependant {
    protected[reactive] def ping(transaction: Transaction): Unit
  }
  trait Dependency {
    protected[reactive] def sourceDependencies(transaction: Transaction): Set[UUID]
    protected[reactive] def isConnectedTo(transaction: Transaction): Boolean
    protected[reactive] def addDependant(transaction: Transaction, dependant: Dependant): Unit
    protected[reactive] def removeDependant(transaction: Transaction, dependant: Dependant): Unit
  }
  trait Observable[+O] {
    def log: Signal[Seq[O]]
    def observe(obs: O => Unit)
    def unobserve(obs: O => Unit)
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