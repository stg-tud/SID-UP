package remote
import reactive.SignalDependant
import util.SerializationSafe
import reactive.Signal

@remote trait RemoteSignal[+A] {
  def addDependant(obs: SignalDependant[A])
  def removeDependant(obs: SignalDependant[A])
}

object RemoteSignal {
  def send[A: SerializationSafe](reactive: Signal[A]): EstablishSignalConnectionData[A] = new RemoteSignalImpl(reactive).makeConnectionData
  def receive[A: SerializationSafe](connectionData: EstablishSignalConnectionData[A]) = new RemoteDependantSignal(connectionData)
}