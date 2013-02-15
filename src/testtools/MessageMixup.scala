package testtools

import scala.collection.mutable
import reactive.Reactive
import reactive.Var
import reactive.Event
import reactive.EventStreamDependant
import reactive.Signal
import reactive.impl.SignalImpl
import reactive.impl.StatelessSignal
import scala.util.Random
import reactive.SignalDependant

class MessageMixup[A](input: Signal[A]) extends StatelessSignal[A]("NetworkMixer[" + input.name + "]", input.now) with SignalDependant[A] {
  input.addDependant(this);
  val messages = mutable.MutableList[(Event, Option[A])]()
  override def sourceDependencies = input.sourceDependencies
  override def notifyEvent(event: Event, value: A, changed: Boolean) {
    //    println("recording new value " + input.value + " for event " + event);
    messages.synchronized {
      messages += ((event, if (changed) Some(value) else None));
    }
  }

  def releaseQueue() {
    Random.shuffle(messages.synchronized {
      val release = messages.toList;
      messages.clear()
      release
    }).foreach {
      case (event, maybeValue) =>
        //      println("releasing new value " + value + " for event " + event);
        propagate(event, maybeValue);
    }
  }
}
