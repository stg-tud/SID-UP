package unoptimized.remote

import unoptimized.Transaction
import java.util.UUID
import java.rmi.Remote

/**
 * This is the remote equivalent to Reactive.Dependant
 */
@remote trait RemoteDependant[V] {
  def update(transaction: Transaction, pulse: Option[V], updatedSourceDependencies: Option[Set[UUID]]): Unit
}
