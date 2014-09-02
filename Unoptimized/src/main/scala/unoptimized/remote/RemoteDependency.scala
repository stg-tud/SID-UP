package unoptimized.remote

import unoptimized.Transaction
import java.util.UUID
import java.rmi.Remote

/**
 * This is the remote equivalent to Reactive.Dependency
 */
@remote trait RemoteDependency[V] {
  protected[unoptimized] def registerRemoteDependant(transaction: Transaction, dependant: RemoteDependant[V]): Set[UUID]
  protected[unoptimized] def unregisterRemoteDependant(transaction: Transaction, dependant: RemoteDependant[V]): Unit
}
