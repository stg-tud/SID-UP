package unoptimized.remote.impl

import unoptimized.remote.RemoteSignalDependency
import unoptimized.signals.impl.SignalImpl
import unoptimized.Transaction

class RemoteSignalSinkImpl[A](dependency: RemoteSignalDependency[A]) extends RemoteSinkImpl[A](dependency) with SignalImpl[A] {

  var now = dependency.value(null)

  protected[unoptimized] def value(transaction: unoptimized.Transaction): A = now

  override def update(transaction: Transaction, pulse: Option[A], updatedSourceDependencies: Option[Set[java.util.UUID]]): Unit = synchronized {
    pulse.foreach(now = _)
    super.update(transaction, pulse, updatedSourceDependencies)
  }
}
