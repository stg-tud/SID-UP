package reactive.remote.impl

import reactive.Reactive
import reactive.Transaction
import reactive.impl.ReactiveImpl
import java.util.UUID
import reactive.remote.{RemoteDependency, RemoteDependant}
import java.rmi.server.UnicastRemoteObject

abstract class RemoteSourceImpl[P] extends UnicastRemoteObject with Reactive.Dependant with RemoteDependency[P] {
  def dependency: Reactive[_, P]

  var dependants: Set[RemoteDependant[P]] = Set()

  dependency.addDependant(null, this)

  override def ping(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = ReactiveImpl.parallelForeach(dependants) {
    _.update(transaction,
      pulse = if (pulsed) dependency.pulse(transaction) else None,
      updatedSourceDependencies = if (sourceDependenciesChanged) Some(dependency.sourceDependencies(transaction)) else None)
  }

  override def registerRemoteDependant(transaction: Transaction, dependant: RemoteDependant[P]): Set[UUID] = {
    dependants += dependant
    dependency.sourceDependencies(transaction)
  }
  override def unregisterRemoteDependant(transaction: Transaction, dependant: RemoteDependant[P]): Unit = {
    dependants -= dependant
  }
}
