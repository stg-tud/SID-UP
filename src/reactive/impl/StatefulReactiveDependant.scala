package reactive.impl
import reactive.ReactiveDependant
import reactive.Reactive
import reactive.Event

trait StatefulReactiveDependant[A] extends ReactiveDependant[A] {
  this : Reactive[_] =>
  private val ordering = new EventOrderingCache[Option[A]](sourceDependencies) {
    override def eventReadyInOrder(event: Event, maybeValue: Option[A]) {
      notifyEventInOrder(event, maybeValue);
    }
  }
  def notifyEventInOrder(event : Event, maybeValue : Option[A]);
  
  override def notifyEvent(event: Event, maybeValue: Option[A]) {
    ordering.eventReady(event, maybeValue);
  }
}