package reactive
import impl.FunctionalSignal
import reactive.impl.SnapshotSignal

trait Signal[+A] extends Reactive[A] {
  // use this to get the current value from regular code
  def now: A
  def apply()(implicit transaction: Transaction): A
  def changes: EventStream[A]
  def map[B](op: A => B): Signal[B]
  def rmap[B](op: A => Signal[B]): Signal[B]
  def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B];
  def snapshot(when: EventStream[_]): Signal[A]
}

