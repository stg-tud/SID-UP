package reactive
import java.util.UUID

@remote trait SignalDependant[-A] {
  def notifyEvent(propagationData : PropagationData, value : A, changed : Boolean);
}