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
    // TODO: "None" theoretically could be propagated to dependants before ordering.
    // The only issue with that is that if a depending signal then recalculates due
    // to other dependencies having changed for the given event, it will request this
    // signal's value for the given event. This request would then have to be blocked
    // until the event has been processed in order, because the value to be reused
    // for the reply would not be known yet at that point.
    ordering.eventReady(event, maybeValue);
  }
}