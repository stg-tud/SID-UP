package reactive

import scala.collection.mutable
import util.Util.nullSafeEqual

abstract class SignalImpl[A](name: String, initialValue: A) extends ReactiveImpl[A](name) with Signal[A] with EventStream[A] {
  private var currentValue = initialValue;
  // TODO: instead of using a WeakHashMap, references on events should be counted explicitly.
  // Using a WeakHashMap works, but retains events unnecessarily long, which irrevokably bloats
  // each map's size. That is however a bunch of work, especially considering there can exist
  // multiple instances of the "same" event through back and forth network transfers
  private val valHistory = new mutable.WeakHashMap[Event, (A, Boolean)]();

  def value = {
    val currentEvent = Signal.threadEvent.get();
    valHistory.synchronized { valHistory.get(currentEvent) }.map { _._1 }.getOrElse(currentValue);
  }

  private def await(event: Event): (A, Boolean) = {
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
  /**
   * suspends the current thread until this reactive has completed processing the given event.
   */
  override def awaitValue(event: Event): A = {
    await(event)._1
  }

  override def awaitMaybeEvent(event: Event): Option[A] = {
    val (value, changed) = await(event);
    if (changed) Some(value) else None
  }

  private var ordering: EventOrderingCache[A] = new EventOrderingCache[A](sourceDependencies) {
    override def eventReadyInOrder(event: Event, newValue: A) {
      val changed = valHistory.synchronized {
        val changed = !nullSafeEqual(currentValue, newValue);
        currentValue = newValue;

        valHistory += (event -> ((newValue, changed)))
        valHistory.notifyAll();
        changed
      }

      if (changed) {
        notifyDependants(event, newValue);
        notifyObservers(event, newValue)
      } else {
        notifyDependants(event);
      }
    }
  }

  protected[this] def updateValue(event: Event, newValue: A) {
    ordering.eventReady(event, newValue);
  }

  override val changes: EventStream[A] = this
  override def snapshot(when: EventStream[_]) = new SnapshotSignal(this, when);
}