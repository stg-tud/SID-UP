package reactive.remote

import java.rmi.server.UnicastRemoteObject

import reactive.signals.impl.SignalImpl
import reactive.events.impl.EventStreamImpl
import reactive.Transaction

class RemoteSignalSinkImpl[A](val dependency: RemoteDependency[A])
  extends UnicastRemoteObject with RemoteDependant[A] with SignalImpl[A] {

  var (Some(now), _sourceDependencies) = dependency.registerRemoteDependant(null, this)

  protected[reactive] def sourceDependencies(transaction: reactive.Transaction): Set[java.util.UUID] = _sourceDependencies
  protected[reactive] def value(transaction: reactive.Transaction): A = now

  def update(transaction: reactive.Transaction, pulse: Option[A], updatedSourceDependencies: Option[Set[java.util.UUID]]): Unit = synchronized {
    pulse.foreach(now = _)
    val sdChanged = updatedSourceDependencies match {
      case Some(usd) if usd != _sourceDependencies =>
        _sourceDependencies = usd
        true
      case _ => false
    }
    doPulse(transaction, sdChanged, pulse)
  }
}

class RemoteEventSinkImpl[A](val dependency: RemoteDependency[A])
  extends UnicastRemoteObject with RemoteDependant[A] with EventStreamImpl[A] {

  var (None, _sourceDependencies) = dependency.registerRemoteDependant(null, this)

  protected[reactive] def sourceDependencies(transaction: reactive.Transaction): Set[java.util.UUID] = _sourceDependencies

  def update(transaction: reactive.Transaction, pulse: Option[A], updatedSourceDependencies: Option[Set[java.util.UUID]]): Unit = synchronized {
    val sdChanged = updatedSourceDependencies match {
      case Some(usd) if usd != _sourceDependencies =>
        _sourceDependencies = usd
        true
      case _ => false
    }
    doPulse(transaction, sdChanged, pulse)
  }
}
