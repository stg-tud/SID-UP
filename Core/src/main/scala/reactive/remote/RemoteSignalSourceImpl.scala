package reactive.remote

import java.rmi.server.UnicastRemoteObject
import reactive.Transaction
import reactive.impl.SingleDependentReactive
import reactive.signals.Signal
import reactive.signals.impl.DependentSignalImpl
import reactive.Reactive
import reactive.impl.ReactiveImpl

class RemoteSignalSourceImpl[A](val dependency: Signal[A])
  extends UnicastRemoteObject with Reactive.Dependant with RemoteSignalDependency[A] {

  dependency.addDependant(null, this)

  def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit = ReactiveImpl.parallelForeach(dependants) {
    _.update(transaction,
      pulse = if (pulsed) dependency.pulse(transaction) else None,
      updatedSourceDependencies = if (sourceDependenciesChanged) Some(dependency.sourceDependencies(transaction)) else None)
  }

  var dependants: Set[RemoteDependant[A]] = Set()
  protected[reactive] def addRemoteDependant(transaction: Transaction, dependant: RemoteDependant[A]) = {
    dependants += dependant
    dependant.update(transaction, Option(dependency.value(transaction)), Option(dependency.sourceDependencies(transaction)))
  }
}
