package reactive
import java.util.UUID

class Event(val sourcesAndPredecessors: Map[UUID, UUID]) extends Serializable {
  val uuid = UUID.randomUUID();

  override def equals(other: Any) = {
    other match {
      case that: Event => that.uuid.equals(uuid)
      case _ => false
    }
  }
  override def hashCode() = {
    31 + uuid.hashCode();
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
