package reactive.remote

import java.util.UUID
import reactive.Reactive
import reactive.Transaction
import reactive.signals.Signal
import reactive.signals.Signal
import java.rmi.Naming
import java.rmi.server.UnicastRemoteObject
import java.rmi.Remote

@remote trait RemoteSignalDependency[V] {
  protected[reactive] def addRemoteDependant(transaction: Transaction, dependant: RemoteDependant[V])
}

@remote trait RemoteDependant[V] {
  def update(transaction: Transaction, pulse: Option[V], updatedSourceDependencies: Option[Set[UUID]]): Unit
}

object RemoteSignal {
  def apply[A](signal: Signal[A]): Remote = new RemoteSignalSourceImpl(signal)
  def apply[A](dependency: RemoteSignalDependency[A]): Signal[A] = new RemoteSignalSinkImpl(dependency)
  def rebind[A](name: String, signal: Signal[A]): Unit = Naming.rebind(name, apply(signal))
  def lookup[A](name: String): Signal[A] = apply(Naming.lookup(name).asInstanceOf[RemoteSignalDependency[A]])
}