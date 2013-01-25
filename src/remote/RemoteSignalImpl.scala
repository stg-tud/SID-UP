package remote
import util.SerializationSafe
import reactive.Signal

class RemoteSignalImpl[A : SerializationSafe](local: Signal[A]) extends RemoteReactiveImpl[A](local) {
 def makeConnectionData = new EstablishSignalConnectionData(this, local.name, local.now, local.sourceDependencies)
}