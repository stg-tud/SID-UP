package reactive
package signals

import reactive.events.EventStream

trait Signal[+A] extends Reactive[A, SignalNotification[A]] {
  def now: A
  def apply()(implicit t: Transaction): A
  def changes: EventStream[A]
  def map[B](op: A => B): Signal[B]
  //  def rmap[B](op: A => Signal[B]): Signal[B]
//  def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B];
  def snapshot(when: EventStream[_]): Signal[A]
}

object Signal {
  type Dependant[-A] = ReactiveDependant[SignalNotification[A]]
}