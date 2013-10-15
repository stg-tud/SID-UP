package reactive.remote

import java.rmi.Naming
import java.rmi.Remote
import java.util.UUID

import reactive.Transaction
import reactive.signals.Signal

@remote trait RemoteDependency[V] {
  protected[reactive] def registerRemoteDependant(transaction: Transaction, dependant: RemoteDependant[V]): (V, Set[UUID])
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