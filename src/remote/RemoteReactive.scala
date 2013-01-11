package remote
import java.util.UUID
import reactive.Event

@remote trait RemoteReactive[A] {
  def name : String
  def value : A
  def sourceDependencies : Map[UUID, UUID]
  def addDependant(obs: RemoteDependant[A])
}