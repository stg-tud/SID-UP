package remote
import java.util.UUID
import util.SerializationSafe

case class EstablishEventStreamConnectionData[A : SerializationSafe](remote : RemoteReactive[A], name : String, sourceDependencies : Map[UUID, UUID])