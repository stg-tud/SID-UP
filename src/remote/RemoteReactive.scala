package remote
import util.SerializationSafe
import reactive.EventStream
import reactive.Signal

@remote trait RemoteReactive[+A] {
  def addDependant(obs: RemoteReactiveDependant[A])
  def removeDependant(obs: RemoteReactiveDependant[A])
}

object RemoteReactive {
  def send[A : SerializationSafe](reactive : EventStream[A]) : EstablishEventStreamConnectionData[A] = new RemoteEventStreamImpl(reactive).makeConnectionData
  def receive[A : SerializationSafe](connectionData : EstablishEventStreamConnectionData[A]) = new RemoteDependantEventStream(connectionData)
  def send[A: SerializationSafe](reactive: Signal[A]): EstablishSignalConnectionData[A] = new RemoteSignalImpl(reactive).makeConnectionData
  def receive[A: SerializationSafe](connectionData: EstablishSignalConnectionData[A]) = new RemoteDependantSignal(connectionData)
}