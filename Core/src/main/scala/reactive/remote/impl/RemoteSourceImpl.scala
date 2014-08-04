package reactive.remote.impl

import java.rmi.server.UnicastRemoteObject
import java.util.UUID

import reactive.{Reactive, Transaction}
import reactive.impl.ReactiveImpl
import reactive.remote.{RemoteDependant, RemoteDependency}

abstract class RemoteSourceImpl[P] extends UnicastRemoteObject with Reactive.Dependant with RemoteDependency[P] {
  def dependency: Reactive[_, P]

  var dependants: Set[RemoteDependant[P]] = Set()

  dependency.addDependant(null, this)

  override def ping(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = ReactiveImpl.parallelForeach(dependants) {
    _.update(transaction,
      pulse = if (pulsed) dependency.pulse(transaction.stmTx).asOption else None,
      updatedSourceDependencies = if (sourceDependenciesChanged) Some(dependency.sourceDependencies(transaction.stmTx)) else None)
  }

  override def registerRemoteDependant(transaction: Transaction, dependant: RemoteDependant[P]): Set[UUID] = {
    dependants += dependant
    dependency.sourceDependencies(transaction.stmTx)
  }
  override def unregisterRemoteDependant(transaction: Transaction, dependant: RemoteDependant[P]): Unit = {
    dependants -= dependant
  }
}
