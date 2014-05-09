package reactive
package events

import reactive.signals.Signal
import scala.concurrent.stm.InTxn

trait EventStream[+A] extends Reactive[A, A] {
  def hold[B >: A](initialValue: B)(implicit inTxn: InTxn): Signal[B]
  def map[B](op: A => B)(implicit inTxn: InTxn): EventStream[B]
  def collect[B](op: PartialFunction[A, B])(implicit inTxn: InTxn): EventStream[B]
  def merge[B >: A](streams: EventStream[B]*)(implicit inTxn: InTxn): EventStream[B]
  def fold[B](initialValue: B)(op: (B, A) => B)(implicit inTxn: InTxn): Signal[B]
  def filter(op: A => Boolean)(implicit inTxn: InTxn): EventStream[A]
  override def single: EventStream.View[A]
}

object EventStream {
  trait View[+A] extends Reactive.View[A] {
    def hold[B >: A](initialValue: B): Signal[B]
    def map[B](op: A => B): EventStream[B]
    def collect[B](op: PartialFunction[A, B]): EventStream[B]
    def merge[B >: A](streams: EventStream[B]*): EventStream[B]
    def fold[B](initialValue: B)(op: (B, A) => B): Signal[B]
    def filter(op: A => Boolean): EventStream[A]
  }
}