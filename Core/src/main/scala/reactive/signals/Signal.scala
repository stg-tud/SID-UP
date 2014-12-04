package reactive
package signals

import reactive.events.EventStream
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.collection.TraversableLike

trait Signal[+A] extends Reactive[A, A] {
  def now: A
  protected[reactive] def value(transaction: Transaction): A
  def changes: EventStream[A]
  def map[B](op: A => B): Signal[B]
  def delta: EventStream[(A, A)]
  def flatMap[B](op: A => Signal[B]): Signal[B]
  def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B];
  def snapshot(when: EventStream[_]): Signal[A]
  def pulse(when: EventStream[_]): EventStream[A]
  def transposeS[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[Signal[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]]): Signal[C[T]]
  def transposeE[T, C[B] <: TraversableLike[B, C[B]]](implicit evidence: A <:< C[EventStream[T]], canBuildFrom: CanBuildFrom[C[_], T, C[T]]): EventStream[C[T]]
}

//object Signal {
//  def opWithCatch[A](op: => A): Either[A, Throwable] = try {
//    Left(op)
//  } catch {
//    case e: Throwable =>
//      Right(e)
//  }
//  def opWithCatch[A, B](param: Either[A, Throwable], op: A => B): Either[B, Throwable] = {
//    param match {
//      case Left(value) => opWithCatch { op(value) }
//      case Right(e) => Right(e)
//    }
//  }
//}