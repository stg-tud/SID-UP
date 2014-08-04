package reactive.remote.impl

import reactive.Transaction
import reactive.remote.RemoteSignalDependency
import reactive.signals.Signal

class RemoteSignalSourceImpl[A](val dependency: Signal[A]) extends RemoteSourceImpl[A] with RemoteSignalDependency[A] {
  override def value(transaction: Transaction): A = dependency.now(transaction.stmTx)
}
