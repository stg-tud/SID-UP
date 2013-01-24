package reactive.impl

import scala.collection.mutable
import util.Util.nullSafeEqual
import reactive.Signal
import reactive.EventStream
import reactive.Event

abstract class SignalImpl[A](name: String, private var currentValue: A) extends ReactiveImpl[A](name) with Signal[A] with EventStream[A] {

  // TODO: instead of using a WeakHashMap, references on events should be counted explicitly.
  // Using a WeakHashMap works, but retains events unnecessarily long, which irrevokably bloats
  // each map's size. That is however a bunch of work, especially considering there can exist
  // multiple instances of the "same" event through back and forth network transfers
  private val valHistory = new mutable.WeakHashMap[Event, (A, Boolean)]();

  def value(currentEvent: Event) = {
    if (currentEvent != null && isConnectedTo(currentEvent)) {
      valHistory.synchronized { valHistory(currentEvent) }._1;
    } else {
      currentValue
    }
  }

  private def await(event: Event): (A, Boolean) = {
    if (!isConnectedTo(event)) {
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

  protected[this] def updateValue(event: Event)(calculateNewValue: A => A) {
    val (newValue, changed) = valHistory.synchronized {
      val newValue = calculateNewValue(currentValue);
      val changed = !nullSafeEqual(currentValue, newValue)
      currentValue = newValue;

      valHistory += (event -> ((newValue, changed)))
      valHistory.notifyAll();
      (newValue, changed)
    }

    if (changed) {
      notifyDependants(event, Some(newValue));
      notifyObservers(event, newValue)
    } else {
      notifyDependants(event, None);
    }
  }

  override val changes: EventStream[A] = this
}