package test

import scala.collection.mutable
import reactive.Reactive
import reactive.Var
import reactive.Event
import reactive.ReactiveDependant
import reactive.Signal
import reactive.SignalImpl

class MessageMixup[A](input: Signal[A]) extends SignalImpl[A]("NetworkMixer[" + input.name + "]", input.value) with ReactiveDependant[A] {
  input.addDependant(this);
  val messages = mutable.MutableList[(Event, Option[A])]()
  override def sourceDependencies = input.sourceDependencies
  override def notifyUpdate(event: Event, value: A) {
//    println("recording new value " + input.value + " for event " + event);
    messages += ((event, Some(value)));
  }
  override def notifyEvent(event: Event) {
    messages += ((event, None));
  }

  def releaseQueue() {
    for ((event, maybeValue) <- messages.reverse) {
//      println("releasing new value " + value + " for event " + event);
      maybeValue match {
        case Some(value) => maybeNewValue(event, value);
        case None => noNewValue(event);
      }
    }
  }
}
