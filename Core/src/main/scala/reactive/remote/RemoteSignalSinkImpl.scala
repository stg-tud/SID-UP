package reactive.remote

import java.rmi.server.UnicastRemoteObject
import java.util.UUID

import reactive.Transaction
import reactive.signals.impl.DependentSignalImpl

class RemoteSignalSinkImpl[A](val dependency: RemoteSignalDependency[A])
  extends UnicastRemoteObject with RemoteDependant with DependentSignalImpl[A] {

  protected def reevaluateValue(transaction: reactive.Transaction): A = dependency.value(transaction)

  dependency.addRemoteDependant(null, this)

  override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean) {
    synchronized {
      doReevaluation(transaction, sourceDependenciesChanged, pulsed)
    }
  }

  protected def calculateSourceDependencies(transaction: Transaction): Set[UUID] = {
    dependency.sourceDependencies(transaction)
  }
}
