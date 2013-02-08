package remote
import util.SerializationSafe
import reactive.Reactive
import reactive.Event
import reactive.Var
import java.rmi.server.UnicastRemoteObject
import java.util.UUID
import reactive.EventStreamDependant
import reactive.Signal
import reactive.impl.SignalImpl
import reactive.impl.StatelessSignal
import reactive.SignalDependant
import reactive.PropagationData

class RemoteDependantSignal[A: SerializationSafe](establishConnectionData: EstablishSignalConnectionData[A]) extends StatelessSignal[A]("remote" + establishConnectionData.name, establishConnectionData.value) {
  val remoteConnection = new UnicastRemoteObject with SignalDependant[A] {
    override def notifyEvent(propagationData : PropagationData, value : A, changed: Boolean) {
      propagate(propagationData, if(changed) Some(value) else None);
    }
  }

  establishConnectionData.remote.addDependant(remoteConnection);
  // TODO: should have order preservation and source dependency updates?
  override def sourceDependencies = establishConnectionData.sourceDependencies
}