package testtools

import reactive.Signal
import reactive.impl.StatelessSignal
import reactive.EventStreamDependant
import reactive.Event
import reactive.SignalDependant
import scala.concurrent._
import ExecutionContext.Implicits.global

class FakeNetwork[A](input: Signal[A]) extends StatelessSignal[A]("NetworkDelayed[" + input.name + "]", input.now) with SignalDependant[A] {
  input.addDependant(this);
  override def sourceDependencies = input.sourceDependencies
  override def notifyEvent(event: Event, value: A, changed: Boolean) {
    future {
      Thread.sleep(500)
      propagate(event, if (changed) Some(value) else None)
    }
  }
}