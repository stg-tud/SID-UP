package remote
import util.SerializationSafe
import reactive.Reactive
import reactive.Event
import reactive.Var
import java.rmi.server.UnicastRemoteObject
import java.util.UUID
import reactive.ReactiveDependant
import reactive.Signal
import reactive.impl.SignalImpl
import reactive.impl.StatelessSignal

class RemoteDependantSignal[A: SerializationSafe](establishConnectionData: EstablishSignalConnectionData[A]) extends StatelessSignal[A]("remote" + establishConnectionData.name, establishConnectionData.value) {
  val remoteConnection = new UnicastRemoteObject with ReactiveDependant[A] {
    override def notifyEvent(event: Event, maybeValue: Option[A]) {
      propagate(event, maybeValue);
    }
  }

  establishConnectionData.remote.addDependant(remoteConnection);
  // TODO: should have order preservation and source dependency updates?
  override def sourceDependencies = establishConnectionData.sourceDependencies
}