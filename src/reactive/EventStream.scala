package reactive

import Reactive._

trait EventStream[+A] extends Reactive[A] {
  def hold[B >: A](initialValue: B)(implicit t: Txn = null): Signal[B]
  def map[B](op: A => B)(implicit t: Txn = null): EventStream[B]
  def merge[B >: A](streams: EventStream[B]*)(implicit t: Txn = null): EventStream[B]
  def fold[B](initialValue: B)(op: (B, A) => B)(implicit t: Txn = null): Signal[B]
  def filter(op: A => Boolean)(implicit t: Txn = null): EventStream[A]
}