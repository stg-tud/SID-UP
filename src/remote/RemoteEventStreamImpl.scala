package remote
import util.SerializationSafe
import reactive.EventStream

class RemoteEventStreamImpl[A : SerializationSafe](local: EventStream[A]) extends RemoteReactiveImpl[A](local) {
 def makeConnectionData = new EstablishEventStreamConnectionData(this, local.name, local.sourceDependencies)
}