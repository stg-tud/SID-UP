package reactive.impl
import reactive.EventStream
import reactive.Transaction
import reactive.EventStreamDependant

class HoldSignal[A](override val changes: EventStream[A], initialValue: A) extends SignalImpl[A]("hold(" + changes.name + ")", initialValue) with EventStreamDependant[A] {
  changes.addDependant(this);
  override def sourceDependencies = changes.sourceDependencies;
  override def notifyEvent(event: Transaction, maybeValue: Option[A]) {
    propagate(event, maybeValue);
  }
}