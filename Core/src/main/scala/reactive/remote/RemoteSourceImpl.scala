package reactive.remote

import java.rmi.server.UnicastRemoteObject
import reactive.Reactive
import reactive.Transaction
import reactive.impl.ReactiveImpl
import reactive.signals.Signal
import java.util.UUID
import reactive.events.EventStream

trait commonUpdateProcessing[P] {
  this: Reactive.Dependant =>

  def dependency: Reactive[_, P]
  var dependants: Set[RemoteDependant[P]] = Set()

  dependency.addDependant(null, this)

  def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = ReactiveImpl.parallelForeach(dependants) {
    _.update(transaction,
      pulse = if (pulsed) dependency.pulse(transaction) else None,
      updatedSourceDependencies = if (sourceDependenciesChanged) Some(dependency.sourceDependencies(transaction)) else None)
  }
}

class RemoteSignalSourceImpl[A](val dependency: Signal[A])
  extends UnicastRemoteObject with Reactive.Dependant with RemoteDependency[A] with commonUpdateProcessing[A] {

  protected[reactive] def registerRemoteDependant(transaction: Transaction, dependant: RemoteDependant[A]): (Option[A], Set[UUID]) = {
    dependants += dependant
    (Some(dependency.value(transaction)), dependency.sourceDependencies(transaction))
  }
}

class RemoteEventSourceImpl[A](val dependency: EventStream[A])
  extends UnicastRemoteObject with Reactive.Dependant with RemoteDependency[A] with commonUpdateProcessing[A] {

  protected[reactive] def registerRemoteDependant(transaction: Transaction, dependant: RemoteDependant[A]): (Option[A], Set[UUID]) = {
    dependants += dependant
    (None, dependency.sourceDependencies(transaction))
  }
}
