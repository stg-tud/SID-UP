package reactive

abstract class StatelessSignal[A](name: String, initialValue: A) extends SignalImpl[A](name, initialValue) {
  private var ordering: EventOrderingCache[Option[A]] = new EventOrderingCache[Option[A]](sourceDependencies) {
    override def eventReadyInOrder(event: Event, maybeNewValue: Option[A]) {
      updateValue(event) { currentValue =>
        maybeNewValue.getOrElse(currentValue);
      };
    }
  }

  protected[this] def propagate(event: Event, maybeValue: Option[A]) {
    ordering.eventReady(event, maybeValue);
  }
}