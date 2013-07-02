package reactive

import java.util.UUID
import reactive.signals.Signal
import util.Update

trait Reactive[+O, +V, +P] {
  def now: V
  protected[reactive] def transientPulse(t: Transaction): Option[ReactiveNotification[P]]
  protected[reactive] def sourceDependencies: Set[UUID]
  protected[reactive] def isConnectedTo(transaction: Transaction): Boolean
  protected[reactive] def addDependant(maybeTransaction: Option[Transaction], dependant: ReactiveDependant[P]): Option[ReactiveNotification[P]]
  protected[reactive] def removeDependant(dependant: ReactiveDependant[P])
  def log: Signal[List[O]]
  def observe(obs: O => Unit)
  def unobserve(obs: O => Unit)
}

//object Reactive {
//  type Signal[+A] = Reactive[A, A, Update[A]]
//  implicit def richSignal[A](signal: Signal[A]) = new Object {
//    def changes: EventStream[A] = null;
//    def map[B](op: A => B): Signal[B] = null;
//    def flatMap[B](op: A => Signal[B]): Signal[B] = null;
//    def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B] = null;
//    def snapshot(when: EventStream[_]): Signal[A] = null;
//  }
//  type EventStream[+A] = Reactive[A, Unit, Option[A]]
//  implicit def richEventStream[A](eventStream: EventStream[A]) = new Object {
//    def hold[B >: A](initialValue: B): Signal[B] = null;
//    def map[B](op: A => B): EventStream[B] = null;
//    def merge[B >: A](streams: EventStream[B]*): EventStream[B] = null;
//    def fold[B](initialValue: B)(op: (B, A) => B): Signal[B] = null;
//    def filter(op: A => Boolean): EventStream[A] = null;
//  }
//}