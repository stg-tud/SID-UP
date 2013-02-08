package reactive.impl
import reactive.Event

abstract class StatelessSignal[A](name: String, initialValue: A) extends SignalImpl[A](name, initialValue) {
  private var ordering: EventOrderingCache[Option[A]] = new EventOrderingCache[Option[A]](sourceDependencies) {
    override def eventReadyInOrder(event: Event, maybeNewValue: Option[A]) {
      updateValue(event) { currentValue =>
        maybeNewValue.getOrElse(currentValue);
      };
    }
  }

  /**
   * @param event the event
   * @param maybeValue Some(value) if the value was recalculated (may still be the
   * same as the current value, this case will be resolved internally) or None if
   * there was no need to recalculate and the old value should be kept.
   */
  protected[this] def propagate(event: Event, maybeValue: Option[A]) {
    ordering.eventReady(event, maybeValue);
  }
}