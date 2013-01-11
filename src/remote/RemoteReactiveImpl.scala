package remote
import java.rmi.server.UnicastRemoteObject
import reactive.Reactive
import scala.collection.mutable.MutableList
import reactive.ReactiveDependant
import reactive.Event
import util.SerializationSafe
import java.util.UUID

class RemoteReactiveImpl[A : SerializationSafe](local: Reactive[A]) extends UnicastRemoteObject with RemoteReactive[A] with ReactiveDependant {
  
  private val remotes = MutableList[RemoteDependant[A]]()
  override def addDependant(remote: RemoteDependant[A]) {
    remotes += remote
  }
  
  local.addDependant(this);
  override def notifyUpdate(event: Event, valueChanged: Boolean) {
    if(valueChanged) {
      val theValue = local.value
      remotes.foreach{ _.notifyUpdate(event, theValue) }
    } else {
      remotes.foreach{ _.notifyEvent(event) }
    }
  }
  
  def makeConnectionData = new EstablishConnectionData(this, local.name, local.value, Map[UUID, UUID]() ++ local.sourceDependencies)
}