package remote
import java.rmi.server.UnicastRemoteObject
import reactive.Reactive
import scala.collection.mutable.MutableList
import reactive.ReactiveDependant
import reactive.Event
import util.SerializationSafe
import java.util.UUID

class RemoteReactiveImpl[A : SerializationSafe](local: Reactive[A]) extends UnicastRemoteObject with RemoteReactive[A] with ReactiveDependant {
  def name = local.name;
  def value = local.value;
  def sourceDependencies = Map[UUID, UUID]() ++ local.sourceDependencies;
  
  private val remotes = MutableList[RemoteDependant[A]]()
  def addDependant(remote: RemoteDependant[A]) {
    remotes += remote
  }
  
  local.addDependant(this);
  def notifyUpdate(event: Event, valueChanged: Boolean) {
    if(valueChanged) {
      val theValue = value
      remotes.foreach{ _.notifyUpdate(event, theValue) }
    } else {
      remotes.foreach{ _.notifyEvent(event) }
    }
  }
}