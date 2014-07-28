package reactive.remote

import reactive.Transaction
import java.util.UUID
import java.rmi.Remote

/**
 * This is the remote equivalent to Reactive.Dependency
 */
@remote trait RemoteDependency[V] {
  protected[reactive] def registerRemoteDependant(transaction: Transaction, dependant: RemoteDependant[V]): Set[UUID]
  protected[reactive] def unregisterRemoteDependant(transaction: Transaction, dependant: RemoteDependant[V]): Unit
}
