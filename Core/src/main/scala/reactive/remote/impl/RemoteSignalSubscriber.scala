package reactive.remote.impl

import reactive.remote.RemoteSignalDependency
import reactive.signals.impl.SignalImpl
import reactive.Transaction
import java.io.ObjectStreamException

class RemoteSignalSubscriber[A](dependency: RemoteSignalDependency[A]) extends RemoteSubscriber[A](dependency) with SignalImpl[A] {

  var now = dependency.value(null)

  override def value(transaction: reactive.Transaction): A = now

  override def update(transaction: Transaction, pulse: Option[A], updatedSourceDependencies: Option[Set[java.util.UUID]]): Unit = synchronized {
    pulse.foreach(now = _)
    super.update(transaction, pulse, updatedSourceDependencies)
  }
  
  @throws(classOf[ObjectStreamException])
  override protected def writeReplace(): Any = this
}
