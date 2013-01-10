package remote
import util.SerializationSafe
import reactive.Reactive
import reactive.Event
import reactive.Var
import java.rmi.server.UnicastRemoteObject

class RemoteDependantReactive[A: SerializationSafe](remote: RemoteReactive[A]) extends UnicastRemoteObject with RemoteDependant[A] {

  class RemoteVar[A](remote : RemoteReactive[A]) extends Reactive[A]("remote" + remote.name, remote.value, remote.knownEvents) {
    override lazy val dirty: Reactive[Boolean] = Var(false);
    def sourceDependencies = remote.sourceDependencies
    def notifyEvent(event: Event) {
      updateValue(event, value);
    }
    def notifyUpdate(event: Event, newValue: A) {
      updateValue(event, newValue);
    }
  }

  val remoteVar = new RemoteVar[A](remote);
  val reactive : Reactive[A] = remoteVar
  
  remote.addDependant(this);
  def notifyEvent(event: Event) {
    remoteVar.notifyEvent(event);
  }
  def notifyUpdate(event: Event, newValue: A) {
    remoteVar.notifyUpdate(event, newValue);
  }

}