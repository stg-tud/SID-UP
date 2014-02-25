package reactive.remote.impl

import reactive.signals.Signal
import reactive.remote.RemoteSignalDependency
import reactive.Transaction

class RemoteSignalSourceImpl[A](val dependency: Signal[A]) extends RemoteSourceImpl[A] with RemoteSignalDependency[A] {
  override def value(transaction: Transaction): A = dependency.value(transaction)
}
