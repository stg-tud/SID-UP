package reactive

import scala.collection.Iterable
import java.util.UUID

@remote trait ReactiveDependant[A] {
  def notifyEvent(event: Event);
  def notifyUpdate(event: Event, newValue: A);
}