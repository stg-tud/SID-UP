package testtools

import scala.concurrent.ops.spawn
import reactive.Signal
import reactive.impl.StatelessSignal
import reactive.EventStreamDependant
import reactive.Event
import reactive.SignalDependant
import reactive.PropagationData

class FakeNetwork[A](input: Signal[A]) extends StatelessSignal[A]("NetworkDelayed[" + input.name + "]", input.now) with SignalDependant[A] {
  input.addDependant(this);
  override def sourceDependencies = input.sourceDependencies
  override def notifyEvent(propagationData : PropagationData, value: A, changed: Boolean) {
    spawn {
      Thread.sleep(500)
      propagate(propagationData, if (changed) Some(value) else None)
    }
  }
}