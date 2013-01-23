package remote
import java.util.UUID
import reactive.Event
import reactive.Reactive
import util.SerializationSafe
import reactive.ReactiveDependant
import java.rmi.server.UnicastRemoteObject
import reactive.EventStream
import reactive.Signal

@remote trait RemoteReactive[+A] {
  def addDependant(obs: ReactiveDependant[A])
  def removeDependant(obs: ReactiveDependant[A])
}

object RemoteReactive {
  def send[A : SerializationSafe](reactive : Signal[A]) : EstablishSignalConnectionData[A] = new RemoteSignalImpl(reactive).makeConnectionData
  def send[A : SerializationSafe](reactive : EventStream[A]) : EstablishEventStreamConnectionData[A] = new RemoteEventStreamImpl(reactive).makeConnectionData
  def receive[A : SerializationSafe](connectionData : EstablishSignalConnectionData[A]) = new RemoteDependantSignal(connectionData)
  def receive[A : SerializationSafe](connectionData : EstablishEventStreamConnectionData[A]) = new RemoteDependantEventStream(connectionData)
}