package reactive.remote.impl

import java.rmi.server.UnicastRemoteObject

import reactive.events.impl.EventStreamImpl
import reactive.Transaction
import reactive.remote.{ RemoteDependant, RemoteDependency }
import reactive.impl.ReactiveImpl

class RemoteSubscriber[P](val dependency: RemoteDependency[P])
  extends UnicastRemoteObject with RemoteDependant[P] {
  this: ReactiveImpl[_, P] =>

  override def hashCode(): Int = dependency.hashCode() + 31
  override def equals(other: Any): Boolean = other.isInstanceOf[RemoteSubscriber[_]] && dependency.equals(other.asInstanceOf[RemoteSubscriber[_]].dependency)

  var _sourceDependencies = dependency.registerRemoteDependant(null, this)

  def disconnect(): Unit = {
    dependency.unregisterRemoteDependant(null, this)
  }

  protected[reactive] def sourceDependencies(transaction: reactive.Transaction): Set[java.util.UUID] = _sourceDependencies

  override def update(transaction: Transaction, pulse: Option[P], updatedSourceDependencies: Option[Set[java.util.UUID]]): Unit = synchronized {
    val sdChanged = updatedSourceDependencies match {
      case Some(usd) if usd != _sourceDependencies =>
        _sourceDependencies = usd
        true
      case _ => false
    }
    doPulse(transaction, sdChanged, pulse)
  }
}
