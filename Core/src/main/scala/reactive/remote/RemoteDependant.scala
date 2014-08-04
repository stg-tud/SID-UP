package reactive.remote

import java.util.UUID

import reactive.Transaction

/**
 * This is the remote equivalent to Reactive.Dependant
 */
@remote trait RemoteDependant[V] {
  def update(transaction: Transaction, pulse: Option[V], updatedSourceDependencies: Option[Set[UUID]]): Unit
}
