package remote

import util.SerializationSafe
import reactive.Signal

class RemoteSignalImpl[A: SerializationSafe](local: Signal[A]) extends RemoteSignal[A] {
  def makeConnectionData = new EstablishSignalConnectionData(this, local.name, local.now, local.sourceDependencies)
  override def addDependant(obs: SignalDependant[A]) {
    local.addDependant(obs);
  }
  override def removeDependant(obs: SignalDependant[A]) {
    local.removeDependant(obs);
  }
}