package reactive
import java.util.UUID

@remote trait EventStreamDependant[-A] {
  def notifyEvent(propagationData : PropagationData, maybeValue : Option[A]);
}