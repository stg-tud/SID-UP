package reactive

import scala.collection.Iterable
import java.util.UUID

trait ReactiveDependant{
  protected[reactive] def notifyUpdate(event: Event, valueChanged: Boolean);
}