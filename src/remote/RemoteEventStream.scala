package remote
import reactive.EventStreamDependant
import util.SerializationSafe
import reactive.EventStream

@remote trait RemoteEventStream[+A] {
  def addDependant(obs: EventStreamDependant[A])
  def removeDependant(obs: EventStreamDependant[A])
}

object RemoteEventStream {
  def send[A : SerializationSafe](reactive : EventStream[A]) : EstablishEventStreamConnectionData[A] = new RemoteEventStreamImpl(reactive).makeConnectionData
  def receive[A : SerializationSafe](connectionData : EstablishEventStreamConnectionData[A]) = new RemoteDependantEventStream(connectionData)
}