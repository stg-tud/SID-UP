package remote
import util.SerializationSafe
import reactive.Reactive
import reactive.Event
import reactive.Var
import java.rmi.server.UnicastRemoteObject
import java.util.UUID
import reactive.ReactiveDependant
import reactive.Signal
import reactive.EventStream
import reactive.ReactiveImpl
import reactive.EventStreamImpl

class RemoteDependantEventStream[A: SerializationSafe](establishConnectionData: EstablishEventStreamConnectionData[A]) extends EventStreamImpl[A]("remote" + establishConnectionData.name) {
  val remoteConnection = new UnicastRemoteObject with ReactiveDependant[A] {
    override def notifyEvent(event: Event) {
      notifyDependants(event)
    }
    override def notifyUpdate(event: Event, newValue: A) {
      notifyDependants(event, newValue);
    }
  }

  establishConnectionData.remote.addDependant(remoteConnection);
  // TODO: should have order preservation and source dependency updates?
  override def sourceDependencies = establishConnectionData.sourceDependencies
}