package test

import scala.collection.mutable
import reactive.Reactive
import reactive.DependantReactive
import reactive.Var
import reactive.Event

class MessageMixup[A](input: Reactive[A]) extends DependantReactive[A]("NetworkMixer[" + input.name + "]", input.value, input) {
  val messages = mutable.MutableList[Tuple2[Event, A]]()
  override lazy val dirty: Reactive[Boolean] = Var(false);
  override def sourceDependencies = input.sourceDependencies
  override def notifyUpdate(event: Event, valueChanged: Boolean) {
          println("recording new value "+input.value+" for event "+event);
    messages += ((event, input.value));
  }

  def releaseQueue() {
    for ((event, value) <- messages.reverse) {
          	println("releasing new value "+value+" for event "+event);
      updateValue(event, value);
    }
  }
}
