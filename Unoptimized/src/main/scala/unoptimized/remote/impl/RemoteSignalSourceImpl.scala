package unoptimized.remote.impl

import unoptimized.signals.Signal
import unoptimized.remote.RemoteSignalDependency
import unoptimized.Transaction

class RemoteSignalSourceImpl[A](val dependency: Signal[A]) extends RemoteSourceImpl[A] with RemoteSignalDependency[A] {
  override def value(transaction: Transaction): A = dependency.value(transaction)
}
