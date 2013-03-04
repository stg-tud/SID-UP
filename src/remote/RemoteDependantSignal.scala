package remote

import util.SerializationSafe
import reactive.Reactive
import reactive.Transaction
import reactive.Var
import java.rmi.server.UnicastRemoteObject
import java.util.UUID
import reactive.Signal
import reactive.impl.SignalImpl

class RemoteDependantSignal[A: SerializationSafe](establishConnectionData: EstablishSignalConnectionData[A]) extends SignalImpl[A]("remote" + establishConnectionData.name, establishConnectionData.value) {
  val remoteConnection = new UnicastRemoteObject with SignalDependant[A] {
    override def notifyEvent(event: Transaction, value : A, changed: Boolean) {
      propagate(event, if(changed) Some(value) else None);
    }
  }

  establishConnectionData.remote.addDependant(remoteConnection);
  // TODO: should have order preservation and source dependency updates?
  override def sourceDependencies = establishConnectionData.sourceDependencies
}