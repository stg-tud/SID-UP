package reactive.impl

import scala.collection.mutable
import util.Util.nullSafeEqual
import reactive.Signal
import reactive.EventStream
import reactive.Event
import scala.actors.threadpool.TimeoutException
import reactive.Event
import reactive.ReactiveDependant

abstract class SignalImpl[A](name: String, private var currentValue: A) extends ReactiveImpl[A](name) with Signal[A] {
  signal =>
  override def now = currentValue
  private var _lastEvent: Event = new Event(Map())
  override def lastEvent = _lastEvent;

  // TODO: instead of using a WeakHashMap, references on events should be counted explicitly.
  // Using a WeakHashMap works, but retains events unnecessarily long, which irrevokably bloats
  // each map's size. That is however a bunch of work, especially considering there can exist
  // multiple instances of the "same" event through back and forth network transfers
  private val valHistory = new mutable.WeakHashMap[Event, (A, Boolean)]();
  valHistory += (lastEvent -> (currentValue, true));

  override def value(ifKnown: Event, otherwise: => Event): A = {
    if (ifKnown == null) {
      currentValue;
    } else {
      valHistory.synchronized {
        valHistory.get(ifKnown).getOrElse(valHistory(otherwise))._1
      }
    }
  }

  @throws(classOf[TimeoutException])
  private def awaitInternal(event: Event, timeout: Long): (A, Boolean) = {
    if (!isConnectedTo(event)) {
      throw new IllegalArgumentException("illegal wait: " + event + " will not update this reactive.");
    }
    valHistory.synchronized {
      var value = valHistory.get(event);
      val end = System.currentTimeMillis() + timeout;
      while (value.isEmpty) {
        if (timeout > 0 && end < System.currentTimeMillis()) throw new TimeoutException(name + " timed out waiting for " + event);
        valHistory.wait(timeout);
        value = valHistory.get(event);
      }
      value
    }.get
  }

  /**
   * suspends the current thread until this reactive has completed processing the given event.
   */
  @throws(classOf[TimeoutException])
  override def await(event: Event, timeout: Long = 0): A = {
    awaitInternal(event, timeout)._1
  }

  protected[this] def updateValue(event: Event)(calculateNewValue: A => A) {
    val (newValue, changed) = valHistory.synchronized {
      val newValue = calculateNewValue(currentValue);
      _lastEvent = event;
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

  override val changes: EventStream[A] = new EventStream[A] {
    override val name = signal.name + ".changes"
    @throws(classOf[TimeoutException])
    override def await(event: Event, timeout: Long = 0): Option[A] = {
      val (value, changed) = awaitInternal(event, timeout);
      if (changed) Some(value) else None
    }
    override def observe(obs: A => Unit) = signal.observe(obs)
    override def unobserve(obs: A => Unit) = signal.unobserve(obs)
    override def sourceDependencies = signal.sourceDependencies
    override def addDependant(dependant: ReactiveDependant[A]) {
      signal.addDependant(dependant)
    }
    override def removeDependant(dependant: ReactiveDependant[A]) {
      signal.addDependant(dependant)
    }
    override def hold[B >: A](initialValue: B): Signal[B] = if (nullSafeEqual(initialValue, currentValue)) signal else new HoldSignal(this, initialValue);
  }
  override def fold[B](initial: A => B)(op: (B, A) => B): Signal[B] = changes.fold(initial(currentValue))(op)
}