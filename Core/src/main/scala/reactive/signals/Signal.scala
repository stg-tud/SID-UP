package reactive
package signals

import reactive.events.EventStream
import scala.concurrent.stm.InTxn
import scala.language.higherKinds
import scala.collection.generic.CanBuildFrom
import scala.collection.TraversableLike

trait Signal[+A] extends Reactive[A, A] {
  override val transactional: Signal.View[A]
  def now: A
  def changes: EventStream[A]
  def map[B](op: A => B): Signal[B]
  def tmap[B](op: (A, InTxn) => B): Signal[B]
  def delta: EventStream[(A, A)]
  def flatMap[B](op: A => Signal[B]): Signal[B]
  def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B]
  def snapshot(when: EventStream[_]): Signal[A]
  def pulse(when: EventStream[_]): EventStream[A]
  def transposeS[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[Signal[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]]): Signal[C[T]]
  def transposeE[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[EventStream[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]]): EventStream[C[T]]
}

object Signal {
  def apply[A1, B](s1: Signal[A1])(f: A1 => B): Signal[B] = Lift.signal1(f)(s1)
  def apply[A1, A2, B](s1: Signal[A1], s2: Signal[A2])(f: (A1, A2) => B): Signal[B] = Lift.signal2(f)(s1, s2)
  def apply[A1, A2, A3, B](s1: Signal[A1], s2: Signal[A2], s3: Signal[A3])(f: (A1, A2, A3) => B): Signal[B] = Lift.signal3(f)(s1, s2, s3)

  trait View[+A] extends Reactive.View[A] {
    def now(implicit inTxn: InTxn): A
    def changes(implicit inTxn: InTxn): EventStream[A]
    def map[B](op: A => B)(implicit inTxn: InTxn): Signal[B]
    def tmap[B](op: (A, InTxn) => B)(implicit inTxn: InTxn): Signal[B]
    def delta(implicit inTxn: InTxn): EventStream[(A, A)]
    def flatMap[B](op: A => Signal[B])(implicit inTxn: InTxn): Signal[B]
    def tflatMap[B](op: (A, InTxn) => Signal[B])(implicit inTxn: InTxn): Signal[B]
    def flatten[B](implicit evidence: A <:< Signal[B], inTxn: InTxn): Signal[B]
    def snapshot(when: EventStream[_])(implicit inTxn: InTxn): Signal[A]
    def pulse(when: EventStream[_])(implicit inTxn: InTxn): EventStream[A]
    def transposeS[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[Signal[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]], inTxn: InTxn): Signal[C[T]]
    def transposeE[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[EventStream[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]], inTxn: InTxn): EventStream[C[T]]
  }
}
