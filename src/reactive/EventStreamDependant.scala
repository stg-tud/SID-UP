package reactive

@remote trait EventStreamDependant[-A] /*extends SignalDependant[A] {
  override def notifyEvent(event : Event, value: A, changed: Boolean) {
    notifyEvent(event, if(changed) Some(value) else None);
  }*/{
  def notifyEvent(event: Event, maybeValue : Option[A]);
}