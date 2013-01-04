package reactive
import java.util.UUID

class Event private (val source : UUID) extends Serializable {
  val uuid = UUID.randomUUID();

  override def equals(other: Any) = {
    other match {
      case that: Event => that.source.equals(source) && that.uuid.equals(uuid)
      case _ => false
    }
  }
  override def hashCode() = {
    1 + 31 * (source.hashCode + 31 * uuid.hashCode())
  }
//  def isComparable(other: Event) = {
//    other.source.equals(source)
//  }
//  /**
//   * note: only meaningful iff {@link #isComparable(Event)}
//   */
//  def happenedBefore(other: Event) = {
//    uuid.timestamp() < other.uuid.timestamp()
//  }
}

object Event {
  def apply(initiator: Var[_]) = new Event(initiator.uuid);
}