package reactive

class HoldSignal[A](override val changes: EventStream[A], initialValue: A) extends SignalImpl[A]("hold(" + changes.name + ")", initialValue) with ReactiveDependant[A] {
  changes.addDependant(this);
  override def sourceDependencies = changes.sourceDependencies;
  override def notifyEvent(event: Event) {
    notifyUpdate(event, value);
  }
  override def notifyUpdate(event: Event, value: A) {
    updateValue(event, value);
  }
  override def hold(initialValue : A) = new HoldSignal(changes, initialValue);
}