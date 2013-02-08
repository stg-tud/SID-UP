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
import java.util.UUID
import reactive.PropagationData

class MessageMixup[A](input: Signal[A]) extends StatelessSignal[A]("NetworkMixer[" + input.name + "]", input.now) with SignalDependant[A] {
  input.addDependant(this);
  val messages = mutable.MutableList[(PropagationData, Option[A])]()
  override def sourceDependencies = input.sourceDependencies
  override def notifyEvent(propagationData : PropagationData, value: A, changed : Boolean) {
    //    println("recording new value " + input.value + " for event " + event);
    messages.synchronized {
      messages += ((propagationData, if (changed) Some(value) else None));
    }
  }

  def releaseQueue() {
    Random.shuffle(messages.synchronized {
      val release = messages.toList;
      messages.clear()
      release
    }).foreach {
      case (propagationData, maybeValue) =>
        //      println("releasing new value " + value + " for event " + event);
        propagate(propagationData, maybeValue);
    }
  }
}
