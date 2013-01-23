package reactive

trait StatefulReactiveDependant[A] extends ReactiveDependant[A] {
  self : Reactive[_] =>
  private val ordering = new EventOrderingCache[Option[A]](sourceDependencies) {
    override def eventReadyInOrder(event: Event, maybeValue: Option[A]) {
      self.eventReadyInOrder(event, maybeValue);
    }
  }
  def eventReadyInOrder(event : Event, maybeValue : Option[A]);
  
  override def notifyEvent(event: Event, maybeValue: Option[A]) {
    ordering.eventReady(event, maybeValue);
  }
}