package remote
import util.SerializationSafe
import reactive.Reactive
import reactive.Event
import reactive.Var
import java.rmi.server.UnicastRemoteObject
import java.util.UUID
import reactive.ReactiveDependant
import reactive.Signal
import reactive.SignalImpl

class RemoteDependantSignal[A: SerializationSafe](establishConnectionData: EstablishSignalConnectionData[A]) extends SignalImpl[A]("remote" + establishConnectionData.name, establishConnectionData.value) {
  val remoteConnection = new UnicastRemoteObject with ReactiveDependant[A] {
    override def notifyEvent(event: Event) {
      noNewValue(event)
    }
    override def notifyUpdate(event: Event, newValue: A) {
      maybeNewValue(event, newValue);
    }
  }

  establishConnectionData.remote.addDependant(remoteConnection);
  // TODO: should have order preservation and source dependency updates?
  override def sourceDependencies = establishConnectionData.sourceDependencies
}