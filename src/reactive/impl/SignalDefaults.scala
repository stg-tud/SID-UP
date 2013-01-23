package reactive.impl
import reactive.Signal
import reactive.EventStream

trait SignalDefaults[A] extends Signal[A] {
  def snapshot(when: EventStream[_]) = new SnapshotSignal(this, when);
}