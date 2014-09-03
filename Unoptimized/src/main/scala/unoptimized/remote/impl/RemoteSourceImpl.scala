package unoptimized.remote.impl

import unoptimized.Reactive
import unoptimized.Transaction
import unoptimized.impl.ReactiveImpl
import java.util.UUID
import unoptimized.remote.{RemoteDependency, RemoteDependant}
import java.rmi.server.UnicastRemoteObject

abstract class RemoteSourceImpl[P] extends UnicastRemoteObject with Reactive.Dependant with RemoteDependency[P] {
  def dependency: Reactive[_, P]

  var dependants: Set[RemoteDependant[P]] = Set()

  dependency.addDependant(null, this)

  override def ping(transaction: Transaction): Unit = ReactiveImpl.parallelForeach(dependants) {
    _.update(transaction,
      pulse = dependency.pulse(transaction),
      updatedSourceDependencies = if (dependency.sourceDependenciesChanged(transaction)) Some(dependency.sourceDependencies(transaction)) else None)
  }

  override def registerRemoteDependant(transaction: Transaction, dependant: RemoteDependant[P]): Set[UUID] = {
    dependants += dependant
    dependency.sourceDependencies(transaction)
  }
  override def unregisterRemoteDependant(transaction: Transaction, dependant: RemoteDependant[P]): Unit = {
    dependants -= dependant
  }
}
