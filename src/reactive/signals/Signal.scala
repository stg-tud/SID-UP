package reactive
package signals

import reactive.events.EventStream
import util.Update

trait Signal[+A] extends Reactive[A, A, Update[A]] {
  def now: A
  def apply()(implicit t: Transaction): A
  def changes: EventStream[A]
  def map[B](op: A => B): Signal[B]
  def flatMap[B](op: A => Signal[B]): Signal[B]
  def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B];
  def snapshot(when: EventStream[_]): Signal[A]
}

object Signal {
  type Notification[A] = ReactiveNotification[Update[A]]
  type Dependant[A] = ReactiveDependant[Update[A]]
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
}