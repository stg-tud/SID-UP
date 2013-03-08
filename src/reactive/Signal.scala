package reactive

import Reactive._

trait Signal[+A] extends Reactive[A] {
  // use this to get the current value from regular code
  def now: A
  def apply()(implicit t: Txn): A
  def changes: EventStream[A]
  def map[B](op: A => B)(implicit t: Txn = null): Signal[B]
  def rmap[B](op: A => Signal[B])(implicit t: Txn = null): Signal[B]
  def flatten[B](implicit evidence: A <:< Signal[B], t : Txn = null): Signal[B];
  def snapshot(when: EventStream[_])(implicit t: Txn = null): Signal[A]
}

