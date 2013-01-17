package remote
import java.rmi.server.UnicastRemoteObject
import reactive.Reactive
import scala.collection.mutable.MutableList
import reactive.Event
import util.SerializationSafe
import java.util.UUID
import reactive.ReactiveDependant

class RemoteReactiveImpl[A : SerializationSafe](local: Reactive[A]) extends UnicastRemoteObject with RemoteReactive[A] {
  override def addDependant(remote: ReactiveDependant[_ >: A]) {
    local.addDependant(remote)
  }
  override def removeDependant(remote: ReactiveDependant[_ >: A]) {
    local.removeDependant(remote)
  }
  
  def makeConnectionData = new EstablishConnectionData(this, local.name, local.value, Map[UUID, UUID]() ++ local.sourceDependencies)
}