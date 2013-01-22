package reactive

import scala.collection.mutable
import util.Util.nullSafeEqual

abstract class SignalImpl[A](name: String, initialValue: A) extends ReactiveImpl[A](name) with Signal[A] with EventStream[A] {
  private var currentValue = initialValue;
  // TODO: instead of using a WeakHashMap, references on events should be counted explicitly.
  // Using a WeakHashMap works, but retains events unnecessarily long, which irrevokably bloats
  // each map's size. That is however a bunch of work, especially considering there can exist
  // multiple instances of the "same" event through back and forth network transfers
  private val valHistory = new mutable.WeakHashMap[Event, A]();

  def value = {
    val currentEvent = Signal.threadEvent.get();
    valHistory.synchronized { valHistory.get(currentEvent) }.getOrElse(currentValue);
  }
  
  /**
   * suspends the current thread until this reactive has completed processing the given event.
   */
  def await(event: Event) = {
    if ((event.sourcesAndPredecessors.keySet & sourceDependencies.keySet).isEmpty) {
      throw new IllegalArgumentException("illegal wait: " + event + " will not update this reactive.");
    }
    valHistory.synchronized {
      var value = valHistory.get(event);
      while (value.isEmpty) {
        valHistory.wait();
        value = valHistory.get(event);
      }
      value
    }.get
  }

  /*
   *  TODO needs to be split into subclasses:
   *   - functional signal -> cache into ordering, calculate if changed once ordered
   *   - stateful signal -> will invoke in order, so just forward (do stateful signals exist?!)  
   */
  protected[this] def updateValue(event: Event, newValue: A) {
    val changed = valHistory.synchronized {
      val changed = !nullSafeEqual(currentValue, newValue);
      currentValue = newValue;

      valHistory += (event -> newValue)
      valHistory.notifyAll();
      changed
    }

    if (changed) {
      notifyDependants(event, newValue);
    } else {
      notifyDependants(event);
    }
  }

  override val changes: EventStream[A] = this
  override def snapshot(when: EventStream[_]) = new SnapshotSignal(this, when);
}