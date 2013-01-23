package reactive

class HoldSignal[A](override val changes: EventStream[A], initialValue: A) extends StatelessSignal[A]("hold(" + changes.name + ")", initialValue) with ReactiveDependant[A] {
  changes.addDependant(this);
  override def sourceDependencies = changes.sourceDependencies;
  override def notifyEvent(event: Event, maybeValue: Option[A]) {
    propagate(event, maybeValue);
  }
  override def hold(initialValue: A) = new HoldSignal(changes, initialValue);
}