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
  val messages = mutable.MutableList[Tuple2[Event, A]]()
  override def sourceDependencies = input.sourceDependencies
  override def notifyUpdate(event: Event, value: A) {
//    println("recording new value " + input.value + " for event " + event);
    messages += ((event, value));
  }
  override def notifyEvent(event: Event) {
    messages += ((event, input.value));
  }

  def releaseQueue() {
    for ((event, value) <- messages.reverse) {
//      println("releasing new value " + value + " for event " + event);
      updateValue(event, value);
    }
  }
}
