package reactive.remote

import java.rmi.server.UnicastRemoteObject
import reactive.Reactive
import reactive.Transaction
import reactive.impl.ReactiveImpl
import reactive.signals.Signal
import java.util.UUID

class RemoteSignalSourceImpl[A](val dependency: Signal[A])
  extends UnicastRemoteObject with Reactive.Dependant with RemoteDependency[A] {

  dependency.addDependant(null, this)

  def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = ReactiveImpl.parallelForeach(dependants) {
    _.update(transaction,
      pulse = if (pulsed) dependency.pulse(transaction) else None,
      updatedSourceDependencies = if (sourceDependenciesChanged) Some(dependency.sourceDependencies(transaction)) else None)
  }

  var dependants: Set[RemoteDependant[A]] = Set()
  protected[reactive] def registerRemoteDependant(transaction: Transaction, dependant: RemoteDependant[A]): (A, Set[UUID]) = {
    dependants += dependant
    (dependency.value(transaction), dependency.sourceDependencies(transaction))
  }
}
