package reactive
package signals
package impl

import java.util.UUID
import reactive.impl.ReactiveImpl
import reactive.events.EventStream
import util.Util
import reactive.events.impl.FilteredEventStream
import reactive.events.impl.MappedEventStream
import reactive.events.impl.ChangesEventStream
import util.MutableValue
import util.MutableValue
import util.TicketAccumulator
import util.Update
import reactive.events.impl.PulseEventStream

trait SignalImpl[A] extends ReactiveImpl[A, A, A] with Signal[A] {

  override lazy val changes: EventStream[A] = new ChangesEventStream(this)
  override def map[B](op: A => B): Signal[B] = new MapSignal(this, op)
  override def flatMap[B](op: A => Signal[B]): Signal[B] = map(op).flatten
  override def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B] = new FlattenSignal(this.asInstanceOf[Signal[Signal[B]]]);
  override def log = new FoldSignal(List(now), changes, ((list: List[A], elem: A) => list :+ elem));
  override def snapshot(when: EventStream[_]): Signal[A] = pulse(when).hold(now);
  override def pulse(when: EventStream[_]): EventStream[A] = new PulseEventStream(this, when);

  protected override def getObserverValue(transaction: Transaction, pulseValue: A) = pulseValue

  //  override def publish(notification: ReactiveNotification[Update[A]], replyChannels: TicketAccumulator.Receiver*) {
  //    super.publish(notification, replyChannels: _*)
  //    if (notification.pulse.changed) {
  //      notifyObservers(notification.pulse.newValue);
  //    }
  //  }
}
