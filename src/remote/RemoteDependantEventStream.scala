package remote

import util.SerializationSafe
import reactive.Reactive
import reactive.Transaction
import reactive.Var
import java.rmi.server.UnicastRemoteObject
import java.util.UUID
import reactive.Signal
import reactive.EventStream
import reactive.impl.ReactiveImpl
import reactive.impl.EventStreamImpl

class RemoteDependantEventStream[A: SerializationSafe](establishConnectionData: EstablishEventStreamConnectionData[A]) extends EventStreamImpl[A]("remote" + establishConnectionData.name) {
  val remoteConnection = new UnicastRemoteObject with EventStreamDependant[A] {
    override def notifyEvent(event: Transaction, maybeValue: Option[A]) {
      propagate(event, maybeValue);
    }
  }

  establishConnectionData.remote.addDependant(remoteConnection);
  // TODO: should have order preservation and source dependency updates?
  override def sourceDependencies = establishConnectionData.sourceDependencies
}