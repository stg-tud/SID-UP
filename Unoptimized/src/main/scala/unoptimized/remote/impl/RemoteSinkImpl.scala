package unoptimized.remote.impl

import java.rmi.server.UnicastRemoteObject

import unoptimized.events.impl.EventStreamImpl
import unoptimized.Transaction
import unoptimized.remote.{RemoteDependant, RemoteDependency}
import unoptimized.impl.ReactiveImpl

class RemoteSinkImpl[P](val dependency: RemoteDependency[P])
  extends UnicastRemoteObject with RemoteDependant[P] {
  this: ReactiveImpl[_, P] =>

  var _sourceDependencies = dependency.registerRemoteDependant(null, this)

  def disconnect(): Unit = {
    dependency.unregisterRemoteDependant(null, this)
  }
  
  protected[unoptimized] def sourceDependencies(transaction: unoptimized.Transaction): Set[java.util.UUID] = _sourceDependencies

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
