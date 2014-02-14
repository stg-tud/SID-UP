package reactive.remote

import java.rmi.Naming
import java.rmi.Remote
import java.util.UUID

import reactive.Transaction
import reactive.signals.Signal
import reactive.events.EventStream

@remote trait RemoteDependency[V] {
  protected[reactive] def registerRemoteDependant(transaction: Transaction, dependant: RemoteDependant[V]): (Option[V], Set[UUID])
}

@remote trait RemoteDependant[V] {
  def update(transaction: Transaction, pulse: Option[V], updatedSourceDependencies: Option[Set[UUID]]): Unit
}

object RemoteSignal {
  def apply[A](signal: Signal[A]): Remote = new RemoteSignalSourceImpl(signal)
  def apply[A](dependency: RemoteDependency[A]): Signal[A] = new RemoteSignalSinkImpl(dependency)
  def rebind[A](name: String, signal: Signal[A]): Unit = Naming.rebind(name, apply(signal))
  def lookup[A](name: String): Signal[A] = apply(Naming.lookup(name).asInstanceOf[RemoteDependency[A]])
}

object RemoteEvent {
  def apply[A](event: EventStream[A]): Remote = new RemoteEventSourceImpl(event)
  def apply[A](dependency: RemoteDependency[A]): EventStream[A] = new RemoteEventSinkImpl(dependency)
  def rebind[A](name: String, signal: EventStream[A]): Unit = Naming.rebind(name, apply(signal))
  def lookup[A](name: String): EventStream[A] = apply(Naming.lookup(name).asInstanceOf[RemoteDependency[A]])
}
