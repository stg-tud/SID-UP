package remote
import reactive.Event
import util.SerializationSafe

@remote trait RemoteDependant[A] {
  def notifyEvent(event: Event);
  def notifyUpdate(event: Event, newValue: A);
}