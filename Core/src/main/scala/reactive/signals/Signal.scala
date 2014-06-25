package reactive
package signals

import reactive.events.EventStream
import scala.concurrent.stm.InTxn

trait Signal[+A] extends Reactive[A, A] {
  override def single: Signal.View[A]
  def now(implicit inTxn: InTxn): A
  def changes(implicit inTxn: InTxn): EventStream[A]
  def map[B](op: A => B)(implicit inTxn: InTxn): Signal[B]
  def tmap[B](op: (A, InTxn) => B)(implicit inTxn: InTxn): Signal[B]
  def delta(implicit inTxn: InTxn): EventStream[(A, A)]
  def flatMap[B](op: A => Signal[B])(implicit inTxn: InTxn): Signal[B]
  def tflatMap[B](op: (A, InTxn) => Signal[B])(implicit inTxn: InTxn): Signal[B]
  def flatten[B](implicit evidence: A <:< Signal[B], inTxn: InTxn): Signal[B];
  def snapshot(when: EventStream[_])(implicit inTxn: InTxn): Signal[A]
  def pulse(when: EventStream[_])(implicit inTxn: InTxn): EventStream[A]
}

object Signal {
  trait View[+A] extends Reactive.View[A] {
    def now: A
    def changes: EventStream[A]
    def map[B](op: A => B): Signal[B]
    def tmap[B](op: (A, InTxn) => B): Signal[B]
    def delta: EventStream[(A, A)]
    def flatMap[B](op: A => Signal[B]): Signal[B]
    def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B];
    def snapshot(when: EventStream[_]): Signal[A]
    def pulse(when: EventStream[_]): EventStream[A]
  }
}