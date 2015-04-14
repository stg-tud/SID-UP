package reactive.remote.impl

import reactive.signals.Signal
import reactive.remote.RemoteSignalDependency
import reactive.Transaction

class RemoteSignalPublisher[A](val dependency: Signal[A]) extends RemotePublisher[A] with RemoteSignalDependency[A] {
  override def value(transaction: Transaction): A = dependency.value(transaction)
}
