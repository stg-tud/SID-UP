package reactive.remote

import reactive.Transaction
import java.rmi.Remote

@remote trait RemoteSignalDependency[V] extends RemoteDependency[V] {
  def value(transaction: Transaction): V
}
