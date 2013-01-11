package remote
import java.util.UUID
import util.SerializationSafe

case class EstablishConnectionData[A : SerializationSafe](remote : RemoteReactive[A], name : String, value : A, sourceDependencies : Map[UUID, UUID])