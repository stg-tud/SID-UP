package reactive.remote

import java.rmi.server.UnicastRemoteObject

import reactive.Transaction
import reactive.impl.SingleDependentReactive
import reactive.signals.Signal
import reactive.signals.impl.DependentSignalImpl

class RemoteSignalSourceImpl[A](override val dependency: Signal[A])
  extends UnicastRemoteObject with DependentSignalImpl[A] with SingleDependentReactive with RemoteSignalDependency[A] {

  override protected def reevaluateValue(transaction: Transaction): A = dependency.value(transaction)

  protected[reactive] def addRemoteDependant(transaction: Transaction, dependant: RemoteDependant) = addDependant(transaction, dependant)
  protected[reactive] def removeRemoteDependant(transaction: Transaction, dependant: RemoteDependant) = addDependant(transaction, dependant)
}
