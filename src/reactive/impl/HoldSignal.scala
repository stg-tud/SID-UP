package reactive.impl
import reactive.EventStream
import reactive.Event
import reactive.ReactiveDependant

class HoldSignal[A](override val changes: EventStream[A], initialValue: A) extends StatelessSignal[A]("hold(" + changes.name + ")", initialValue) with ReactiveDependant[A] {
  changes.addDependant(this);
  override def sourceDependencies = changes.sourceDependencies;
  override def notifyEvent(event: Event, maybeValue: Option[A]) {
    propagate(event, maybeValue);
  }
}