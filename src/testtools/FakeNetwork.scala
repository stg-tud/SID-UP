package testtools

import scala.concurrent.ops.spawn
import reactive.Signal
import reactive.impl.StatelessSignal
import reactive.ReactiveDependant
import reactive.Event

class FakeNetwork[A](input: Signal[A]) extends StatelessSignal[A]("NetworkDelayed[" + input.name + "]", input.value) with ReactiveDependant[A] {
  input.addDependant(this);
  override def sourceDependencies = input.sourceDependencies
  override def notifyEvent(event: Event, value: Option[A]) {
    spawn {
      Thread.sleep(500)
      propagate(event, value)
    }
  }
}