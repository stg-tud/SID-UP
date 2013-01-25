package testtools

import scala.collection.mutable
import reactive.Reactive
import reactive.Var
import reactive.Event
import reactive.ReactiveDependant
import reactive.Signal
import reactive.impl.SignalImpl
import reactive.impl.StatelessSignal

class MessageMixup[A](input: Signal[A]) extends StatelessSignal[A]("NetworkMixer[" + input.name + "]", input.now) with ReactiveDependant[A] {
  input.addDependant(this);
  val messages = mutable.MutableList[(Event, Option[A])]()
  override def sourceDependencies = input.sourceDependencies
  override def notifyEvent(event: Event, maybeValue: Option[A]) {
    //    println("recording new value " + input.value + " for event " + event);
    messages += ((event, maybeValue));
  }

  def releaseQueue() {
    messages.reverse.foreach {
      case (event, maybeValue) =>
        //      println("releasing new value " + value + " for event " + event);
        propagate(event, maybeValue);
    }
  }
}
