package remote
import util.SerializationSafe
import reactive.EventStream
import reactive.EventStreamDependant

class RemoteEventStreamImpl[A: SerializationSafe](local: EventStream[A]) extends RemoteEventStream[A] {
  def makeConnectionData = new EstablishEventStreamConnectionData(this, local.name, local.sourceDependencies)
  override def addDependant(obs: EventStreamDependant[A]) {
    local.addDependant(obs);
  }
  override def removeDependant(obs: EventStreamDependant[A]) {
    local.removeDependant(obs);
  }
}