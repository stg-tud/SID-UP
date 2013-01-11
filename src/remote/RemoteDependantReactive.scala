package remote
import util.SerializationSafe
import reactive.Reactive
import reactive.Event
import reactive.Var
import java.rmi.server.UnicastRemoteObject
import java.util.UUID

class RemoteDependantReactive[A: SerializationSafe](establishConnectionData: EstablishConnectionData[A]) extends Reactive[A]("remote" + establishConnectionData.name, establishConnectionData.value) {
  val remoteConnection = new UnicastRemoteObject with RemoteDependant[A] {
    override def notifyEvent(event: Event) {
      updateValue(event, value);
    }
    override def notifyUpdate(event: Event, newValue: A) {
      updateValue(event, newValue);
    }
  }

  establishConnectionData.remote.addDependant(remoteConnection);
  override lazy val dirty: Reactive[Boolean] = Var(false);
  // TODO: should have order preservation and source dependency updates?
  override def sourceDependencies = establishConnectionData.sourceDependencies
}