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

  private var ordering: EventOrderingCache[Option[A]] = new EventOrderingCache[Option[A]](sourceDependencies) {
    override def eventReadyInOrder(event: Event, maybeNewValue: Option[A]) {
      val (newValue, changed) = valHistory.synchronized {
        val (newValue, changed) = maybeNewValue match {
          case Some(newValue) => (newValue, !nullSafeEqual(currentValue, newValue))
          case None => (value, false)
        }
        currentValue = newValue;

        if(name.equals("B1")) println(newValue+" from "+event)
        valHistory += (event -> ((newValue, changed)))
        valHistory.notifyAll();
        (newValue, changed)
      }

      if (changed) {
        notifyDependants(event, newValue);
        notifyObservers(event, newValue)
      } else {
        notifyDependants(event);
      }
    }
  }

  protected[this] def maybeNewValue(event: Event, newValue: A) {
    ordering.eventReady(event, Some(newValue));
  }
  protected[this] def noNewValue(event: Event) {
    ordering.eventReady(event, None);
  }

  override val changes: EventStream[A] = this
  override def snapshot(when: EventStream[_]) = new SnapshotSignal(this, when);
}