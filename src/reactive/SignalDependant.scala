package reactive

@remote trait SignalDependant[-A] {
  def notifyEvent(event: Event, value : A, changed : Boolean);
}