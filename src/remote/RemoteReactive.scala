package remote
import java.util.UUID
import reactive.Event

@remote trait RemoteReactive[A] {
  def name : String
  def value : A
  def sourceDependencies : Map[UUID, UUID]
  def knownEvents : Iterable[Event]
  def addDependant(obs: RemoteDependant[A])
}