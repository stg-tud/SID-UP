package reactive
package signals

import reactive.events.EventStream

trait Signal[+A] extends Reactive[A, Reactive.IDENTITY, Reactive.IDENTITY, Reactive.IDENTITY, Signal] {
  def changes: EventStream[A]
  def map[B](op: A => B): Signal[B]
  def flatMap[B](op: A => Signal[B]): Signal[B]
  def snapshot(when: EventStream[_]): Signal[A]
  def pulse(when: EventStream[_]): EventStream[A]
  import scala.language.higherKinds
  def flatten[X, OW[+_], VW[+_], PW[+_], R[+Y] <: Reactive[Y, OW, VW, PW, R]](implicit evidence: A <:< R[X]): R[X];
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