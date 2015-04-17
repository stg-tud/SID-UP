package reactive.remote.impl

import java.rmi.server.UnicastRemoteObject
import reactive.events.impl.EventStreamImpl
import reactive.Transaction
import reactive.remote.{ RemoteDependant, RemoteDependency }
import reactive.impl.ReactiveImpl
import java.util.UUID

class RemoteSubscriber[P](val dependency: RemoteDependency[P]) extends RemoteDependant[P] {
  self: ReactiveImpl[_, P] =>

  val actualSubscriber = new UnicastRemoteObject with RemoteDependant[P] {
    override def update(transaction: Transaction, pulse: Option[P], updatedSourceDependencies: Option[Set[UUID]]): Unit = {
      self.update(transaction, pulse, updatedSourceDependencies)
    }
  }

  override def hashCode(): Int = dependency.hashCode() + 31
  override def equals(other: Any): Boolean = other.isInstanceOf[RemoteSubscriber[_]] && dependency.equals(other.asInstanceOf[RemoteSubscriber[_]].dependency)

  var _sourceDependencies = dependency.registerRemoteDependant(null, actualSubscriber)

  def disconnect(): Unit = {
    dependency.unregisterRemoteDependant(null, actualSubscriber)
  }

  protected[reactive] def sourceDependencies(transaction: reactive.Transaction): Set[UUID] = _sourceDependencies

  override def update(transaction: Transaction, pulse: Option[P], updatedSourceDependencies: Option[Set[UUID]]): Unit = synchronized {
    val sdChanged = updatedSourceDependencies match {
      case Some(usd) if usd != _sourceDependencies =>
        _sourceDependencies = usd
        true
      case _ => false
    }
    doPulse(transaction, sdChanged, pulse)
  }
}
