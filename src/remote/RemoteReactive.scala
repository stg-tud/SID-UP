package remote
import java.util.UUID
import reactive.Event
import reactive.Reactive
import util.SerializationSafe
import reactive.ReactiveDependant
import java.rmi.server.UnicastRemoteObject

@remote trait RemoteReactive[A] {
  def addDependant(obs: ReactiveDependant[_ >: A])
  def removeDependant(obs: ReactiveDependant[_ >: A])
}

object RemoteReactive {
  def send[A : SerializationSafe](reactive : Reactive[A]) : EstablishConnectionData[A] = new RemoteReactiveImpl(reactive).makeConnectionData
  def receive[A : SerializationSafe](connectionData : EstablishConnectionData[A]) = new RemoteDependantReactive(connectionData)
}