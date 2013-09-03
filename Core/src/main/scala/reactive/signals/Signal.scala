package reactive
package signals

import reactive.events.EventStream
import reactive.impl.mirroring.SignalMirror

trait Signal[+A] extends Reactive[A, A, A, Signal[A]] {
  def changes: EventStream[A]
  def map[B](op: A => B): Signal[B]
  def flatMap[B](op: A => Signal[B]): Signal[B]
  def flatten[R <: Reactive[_, _, _, _]](implicit evidence: A <:< Reactive[_, _, _, R]): R;
  def snapshot(when: EventStream[_]): Signal[A]
  def pulse(when: EventStream[_]): EventStream[A]
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