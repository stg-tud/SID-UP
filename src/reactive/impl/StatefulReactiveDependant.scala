package reactive.impl
import reactive.EventStreamDependant
import reactive.Reactive
import reactive.Event
import reactive.PropagationData

trait StatefulReactiveDependant[A] extends EventStreamDependant[A] {
  this : Reactive[_] =>
  private val ordering = new EventOrderingCache[Option[A]](sourceDependencies) {
    override def eventReadyInOrder(propagationData : PropagationData, maybeValue: Option[A]) {
      notifyEventInOrder(propagationData, maybeValue);
    }
  }
  def notifyEventInOrder(propagationData : PropagationData, maybeValue : Option[A]);
  
  override def notifyEvent(propagationData : PropagationData, maybeValue: Option[A]) {
    ordering.eventReady(propagationData, maybeValue);
  }
}