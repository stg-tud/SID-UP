package reactive.remote

import reactive.Transaction

@remote trait RemoteSignalDependency[V] extends RemoteDependency[V] {
  def value(transaction: Transaction): V
}
