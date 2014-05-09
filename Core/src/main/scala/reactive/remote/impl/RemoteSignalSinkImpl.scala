package reactive.remote.impl

import reactive.remote.RemoteSignalDependency
import reactive.signals.impl.SignalImpl
import reactive.Transaction
import scala.concurrent.stm._

class RemoteSignalSinkImpl[A](dependency: RemoteSignalDependency[A]) extends RemoteSinkImpl[A](dependency) with SignalImpl[A] {
  self =>

  override protected val value = Ref(dependency.value(null))

  override def update(transaction: Transaction, pulse: Option[A], updatedSourceDependencies: Option[Set[java.util.UUID]]): Unit = synchronized {
    pulse.foreach(value.set(_)(transaction.stmTx))
    super.update(transaction, pulse, updatedSourceDependencies)
  }
  
  override object single extends SignalImpl.ViewImpl[A] with RemoteSinkImpl.ViewImpl[A] {
    override val impl = self
  }
}
